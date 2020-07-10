
package com.gitrepositoryaccess.github;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

import com.gitrepositoryaccess.model.GitHubList;
import com.gitrepositoryaccess.model.GitHubSheet;
import com.gitrepositoryaccess.util.UtilityFile;

import fun.mike.dmp.Diff;
import fun.mike.dmp.DiffMatchPatch;

public class GitHubFileCompareWithPreviousVersion {
	private static XSSFSheet sheet;
	private static FileOutputStream outputStream;
	private static XSSFWorkbook workbook;
	private static String fileName, status, modifiedBy, commitMessage;
	private static Date modifiedOn;
	private static Properties props;

	private final static Logger logger = Logger.getLogger(GitHubFileCompareWithPreviousVersion.class.getName());

	public static void main(String[] args) throws IOException {
		GHRepository repository = null;
		props = UtilityFile.getValuesFromProperty();
		props.getProperty("login");
		props.getProperty("password");
		String gitRepoUrl = props.getProperty("gitrepo");
		try {
			createExcelFile();
			GitHub gitHub = GitHubBuilder.fromProperties(props).build();
			repository = gitHub.getRepository(gitRepoUrl);
			Iterator<GHCommit> allCommits = getAllCommits(repository);

			if (allCommits.hasNext()) {
				while (allCommits.hasNext()) {
					GHCommit commit = allCommits.next();
					List<File> fileList = commit.getFiles();
					modifiedOn = commit.getCommitDate();
					modifiedBy = commit.getCommitShortInfo().getAuthor().getName();
					commitMessage = commit.getCommitShortInfo().getMessage();
					for (File file : fileList) {
						List<GitHubList> allList = new ArrayList<GitHubList>();
						GitHubList gitHubList = new GitHubList();
						if (file.getStatus().equals("removed")) {
							fileName = file.getFileName();
							status = file.getStatus();
							gitHubList.setEqualData(Arrays.asList("NA"));
							gitHubList.setInsertedData(Arrays.asList("NA"));
							gitHubList.setDeleteData(Arrays.asList("File Removed"));
							allList.add(gitHubList);
							writeExcel(allList);

						}
					}

					List<String> parentSHA1s = commit.getParentSHA1s();
					GHCompare compare = repository.getCompare(parentSHA1s.get(0), commit.getSHA1());
					URL diffUrl = compare.getDiffUrl();

					HttpURLConnection httpcon = (HttpURLConnection) new URL(diffUrl.toString()).openConnection();
					httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
					BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

					StringBuilder responseSB = new StringBuilder();
					String line, plusLine;
					StringBuilder deletedLines = new StringBuilder();
					StringBuilder addedLines = new StringBuilder();

					while ((line = in.readLine()) != null) {
						responseSB.append("\n" + line);
						if (line.startsWith("-") || line.startsWith("+") || (line.startsWith("diff --git a/"))) {
							if (line.startsWith("-")) {
								if (!(line.startsWith("--- a/"))) {
									deletedLines.append("\n" + line);
								}
								continue;
							}
							if ((line.startsWith("+++ b/"))) {
								fileName = line;
								for (File file : fileList) {
									String substring = fileName.substring(6, fileName.length());
									if (file.getFileName().contains(substring)) {
										status = file.getStatus();
									}
								}
								continue;
							}
							if (!(line.startsWith("diff --git a/"))) {
								addedLines.append("\n" + line);
							}
							while ((plusLine = in.readLine()) != null) {
								if (plusLine.startsWith("+")) {
									addedLines.append("\n" + plusLine);
									continue;

								} else if ((plusLine.startsWith("-")) || (plusLine.startsWith("diff --git a/"))) {
									if ((!deletedLines.toString().isEmpty()) || (!addedLines.toString().isEmpty())) {
										DiffMatchPatch dmp = new DiffMatchPatch();
										LinkedList<Diff> diffs = dmp.diff_main(deletedLines.toString(),
												addedLines.toString());
										addDataInExcel(diffs);
										deletedLines.setLength(0);
										addedLines.setLength(0);
										if (!(plusLine.startsWith("--- a/"))
												&& (!(plusLine.startsWith("diff --git a/")))) {
											deletedLines.append("\n" + plusLine);
										}
									}
									break;
								}
							}
						}
					}
					in.close();
					System.out.println("\n");
					if (!allCommits.hasNext()) {
						logger.info("Excel file has been written sucessfully at location :"
								+ props.getProperty("filePath"));
					}
				}
			} else {
				logger.info("No such commits on this Date...!");
			}
		} catch (IOException exception) {
			logger.info("Not able to connect to the url due to network connection :" + exception);
		}
	}

	private static Iterator<GHCommit> getAllCommits(GHRepository repository) {
		try {
			Date since = null, until = null;
			String date;
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter the Date :");
			date = scanner.next();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			since = dateFormat.parse(date);

			Calendar cal = Calendar.getInstance();
			cal.setTime(since);

			final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
			String timeInterval = "23:59";
			Date time;

			time = simpleDateFormat1.parse(timeInterval);
			cal.set(Calendar.HOUR_OF_DAY, time.getHours());
			cal.set(Calendar.MINUTE, time.getMinutes());
			until = cal.getTime();

			GHCommitQueryBuilder queryBuilder = repository.queryCommits().since(since).until(until);
			PagedIterable<GHCommit> commits = queryBuilder.list();
			Iterator<GHCommit> iterator = commits.iterator();

			return iterator;
		} catch (ParseException e) {
			throw new org.apache.http.ParseException("Please enter date in dd-MM-yyyy format : " + e);
		}
	}

	private static void addDataInExcel(LinkedList<Diff> difference) throws IOException {
		List<String> equalList = new ArrayList<String>();
		List<String> deleteList = new ArrayList<String>();
		List<String> insertList = new ArrayList<String>();
		GitHubSheet gitSheetData = new GitHubSheet();
		for (Diff diffs : difference) {
			if ((diffs.operation).equals(diffs.operation.EQUAL)) {
				gitSheetData.setEqual(diffs.text);
				equalList.add(diffs.text);
			} else if ((diffs.operation).equals(diffs.operation.INSERT)) {
				gitSheetData.setInsert(diffs.text);
				insertList.add(diffs.text);
			} else if ((diffs.operation).equals(diffs.operation.DELETE)) {
				gitSheetData.setDelete(diffs.text);
				deleteList.add(diffs.text);
			}
		}

		List<GitHubList> allList = new ArrayList<GitHubList>();
		GitHubList gitHubList = new GitHubList();
		gitHubList.setEqualData(equalList);
		gitHubList.setInsertedData(insertList);
		gitHubList.setDeleteData(deleteList);
		allList.add(gitHubList);

		writeExcel(allList);
	}

	private static void writeExcel(List<GitHubList> listData) throws IOException {
		int rowCount = sheet.getPhysicalNumberOfRows() + 1;
		for (GitHubList aBook : listData) {
			Row row = sheet.createRow(++rowCount);
			writeBook(aBook, row);
			workbook.write(outputStream);
		}
	}

	private static void writeBook(GitHubList aBook, Row row) {
		try {
			CreationHelper creationHelper = workbook.getCreationHelper();

			Cell cell = row.createCell(1);
			String fullFileName = fileName.substring(6, fileName.length());
			cell.setCellValue(fullFileName);

			cell = row.createCell(2);
			cell.setCellValue(modifiedOn);
			CellStyle style1 = workbook.createCellStyle();
			style1.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy hh:mm:ss"));
			cell.setCellStyle(style1);

			cell = row.createCell(3);
			cell.setCellValue(aBook.getEqualData().toString());

			cell = row.createCell(4);
			cell.setCellValue(aBook.getInsertedData().toString());

			cell = row.createCell(5);
			cell.setCellValue(aBook.getDeleteData().toString());

			cell = row.createCell(6);
			cell.setCellValue(modifiedBy);

			cell = row.createCell(7);
			cell.setCellValue(status);

			cell = row.createCell(8);
			cell.setCellValue(commitMessage);

		} catch (Exception e) {
			logger.info("An Exception occured while writing Excel" + e);
		}
	}

	private static void createExcelFile() {
		try {
			outputStream = new FileOutputStream(props.getProperty("filePath"));
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Modified Files");

			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setColor(IndexedColors.BLACK.index);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);

			Row row = sheet.createRow(0);
			Cell cell = row.createCell(1);
			cell.setCellValue("File Name");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(2);
			cell.setCellValue("Modified On");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(3);
			cell.setCellValue("Equal Lines");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(4);
			cell.setCellValue("Inserted Lines");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(5);
			cell.setCellValue("Deleted Lines");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(6);
			cell.setCellValue("Modified By");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(7);
			cell.setCellValue("Status");
			cell.setCellStyle(headerStyle);

			cell = row.createCell(8);
			cell.setCellValue("Commit Message");
			cell.setCellStyle(headerStyle);

			sheet.createFreezePane(0, 1);

		} catch (Exception e) {
			logger.info("An Exception occured while creating Excel" + e);
		}
	}

}