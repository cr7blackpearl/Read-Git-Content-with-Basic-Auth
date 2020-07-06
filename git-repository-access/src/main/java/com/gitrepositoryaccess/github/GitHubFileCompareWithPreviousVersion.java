
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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

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

	public static void main(String[] args) throws IOException {
		GHRepository repository = null;
		Date since = null, until = null;
		String date;
		Properties props = UtilityFile.getValuesFromProperty();
		props.getProperty("login");
		props.getProperty("password");
		try {
			createExcelFile();
			GitHub gitHub = GitHubBuilder.fromProperties(props).build();

			repository = gitHub.getRepository("cr7blackpearl/Read-Git-Content-with-Basic-Auth");
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
		} catch (ParseException e) {
			throw new org.apache.http.ParseException("Please enter date in dd-MM-yyyy format : " + e);
		}

		GHCommitQueryBuilder queryBuilder = repository.queryCommits().since(since).until(until);
		PagedIterable<GHCommit> commits = queryBuilder.list();
		Iterator<GHCommit> iterator = commits.iterator();

		while (iterator.hasNext()) {
			GHCommit commit = iterator.next();
			System.out.println("Commit: " + commit.getSHA1() + ", info: " + commit.getCommitShortInfo().getMessage()
					+ ", author: " + commit.getCommitShortInfo().getAuthor().getName());
			System.out.println("File Name :" + commit.getFiles().get(0).getFileName());
			System.out.println("File Status :" + commit.getFiles().get(0).getStatus());
			System.out.println("Previous Version:");
			System.out.println("Current Version :");
			System.out.println("Modified Date :" + commit.getCommitDate());
			modifiedOn = commit.getCommitDate();
			System.out.println("Modified By :" + commit.getCommitShortInfo().getAuthor().getName());
			modifiedBy = commit.getCommitShortInfo().getAuthor().getName();
			System.out.println("Project Name :" + commit.getOwner().getName());
			System.out.println("Commit Message :" + commit.getCommitShortInfo().getMessage());
			commitMessage = commit.getCommitShortInfo().getMessage();

			List<String> parentSHA1s = commit.getParentSHA1s();
			System.out.println("Parent SHA :" + parentSHA1s);
			GHCompare compare = repository.getCompare(parentSHA1s.get(0), commit.getSHA1());
			URL diffUrl = compare.getDiffUrl();
			System.out.println("****** Main Url ******: " + diffUrl);

			HttpURLConnection httpcon = (HttpURLConnection) new URL(diffUrl.toString()).openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

			// Read line by line
			StringBuilder responseSB = new StringBuilder();
			String line, plusLine;
			StringBuilder deletedLines = new StringBuilder();
			StringBuilder addedLines = new StringBuilder();

			while ((line = in.readLine()) != null) {
				responseSB.append("\n" + line);
				if (line.startsWith("-") || line.startsWith("+")) {
					if (line.startsWith("-")) {
						if (!(line.startsWith("--- a/"))) {
							deletedLines.append("\n" + line);
						}
						continue;
					}
					if ((line.startsWith("+++ b/"))) {
						fileName = line;
						System.out.println(fileName);
						continue;
					}
					addedLines.append("\n" + line);
					while ((plusLine = in.readLine()) != null) {
						if (plusLine.startsWith("+")) {
							addedLines.append("\n" + plusLine);
							continue;

						} else if (plusLine.startsWith("-")) {
							if ((!deletedLines.toString().isEmpty()) && (!addedLines.toString().isEmpty())) {
								DiffMatchPatch dmp = new DiffMatchPatch();
								LinkedList<Diff> diffs = dmp.diff_main(deletedLines.toString(), addedLines.toString());
								System.out.println(diffs);
								addDataInExcel(diffs);
								deletedLines.setLength(0);
								addedLines.setLength(0);
								if (!(plusLine.startsWith("--- a/"))) {
									deletedLines.append("\n" + plusLine);
								}
							}
							break;
						}
					}
				}
				System.out.println(line);
			}
			System.out.println("Deleted line in the Files : " + deletedLines);
			in.close();
			System.out.println("\n");
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
		System.out.println("Equal Data :" + equalList);
		System.out.println("Deleted Data :" + deleteList);
		System.out.println("Inserted Data :" + insertList);

		List<GitHubList> allList = new ArrayList<GitHubList>();
		GitHubList gitHubList = new GitHubList();
		gitHubList.setEqualData(equalList);
		gitHubList.setInsertedData(insertList);
		gitHubList.setDeleteData(deleteList);
		allList.add(gitHubList);

		writeExcel(allList);
	}

	private static void writeExcel(List<GitHubList> listData) throws IOException {
		System.out.println(sheet.getPhysicalNumberOfRows());
		int rowCount = sheet.getPhysicalNumberOfRows() + 1;
		for (GitHubList aBook : listData) {
			Row row = sheet.createRow(++rowCount);
			writeBook(aBook, row);
			workbook.write(outputStream);
		}
		System.out.println("Current Occupied rows in Excel: " + sheet.getPhysicalNumberOfRows());

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
			style1.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
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
			cell.setCellValue(commitMessage);

		} catch (Exception e) {
			System.out.println("An Exception occured while writing Excel" + e);
		}

	}

	private static void createExcelFile() {
		try {
			outputStream = new FileOutputStream("/home/rohit/Way To Data Science/Excel File/GitHubPlugin.xls");
			// Create blank workbook
			workbook = new XSSFWorkbook();

			// Create a blank sheet
			sheet = workbook.createSheet("Modified Files");

			// We want to make it bold with a foreground color.
			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setColor(IndexedColors.BLACK.index);

			// Create a CellSytle with the font
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);

			// Create the header row
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
			cell.setCellValue("Commit Message");
			cell.setCellStyle(headerStyle);

			sheet.createFreezePane(0, 1);

		} catch (Exception e) {
			System.out.println("An Exception occured while creating Excel" + e);
		}
	}

}
