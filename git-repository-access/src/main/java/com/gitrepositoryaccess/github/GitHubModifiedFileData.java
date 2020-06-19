package com.gitrepositoryaccess.github;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

import com.gitrepositoryaccess.modifiedfilesondate.ReadListOfModifiedFilesOnDate;
import com.gitrepositoryaccess.util.UtilityFile;

public class GitHubModifiedFileData {
	private final static Logger logger = Logger.getLogger(ReadListOfModifiedFilesOnDate.class.getName());

	public static void main(String[] args) {
		GHRepository repository = null;
		Date since = null, until = null;
		try {
			logger.info("Request to Get Files with modified content");
			Properties props = UtilityFile.getValuesFromProperty();
			props.getProperty("login");
			props.getProperty("password");

			GitHub gitHub = GitHubBuilder.fromProperties(props).build();
			repository = gitHub.getRepository("cr7blackpearl/Read-Git-Content-with-Basic-Auth");

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter the Date :");
			String date = scanner.next();
			logger.info("Files modified on: " + date);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			since = dateFormat.parse(date);

			Calendar cal = Calendar.getInstance();
			cal.setTime(since);

			final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
			String timeInterval = "23:59";
			Date time = simpleDateFormat1.parse(timeInterval);
			cal.set(Calendar.HOUR_OF_DAY, time.getHours());
			cal.set(Calendar.MINUTE, time.getMinutes());
			until = cal.getTime();
		} catch (ParseException e) {
			throw new org.apache.http.ParseException("Please enter date in dd-MM-yyyy format : " + e);
		} catch (IOException e) {
			e.printStackTrace();
		}

		GHCommitQueryBuilder queryBuilder = repository.queryCommits().since(since).until(until);
		PagedIterable<GHCommit> commits = queryBuilder.list();
		Iterator<GHCommit> iterator = commits.iterator();
		List<File> files = new ArrayList<>();
		while (iterator.hasNext()) {
			GHCommit commit = iterator.next();
			try {
				System.out.println("Commit: " + commit.getSHA1() + ", info: " + commit.getCommitShortInfo().getMessage()
						+ ", author: " + commit.getCommitShortInfo().getAuthor().getName());
				GHCommit gitCommit = repository.getCommit(commit.getSHA1());
				files = gitCommit.getFiles();
				for (File file : files) {
					logger.info("File Name: " + file.getFileName());
					System.out.println("Commited File SHA: " + file.getSha());
					logger.info("File wise Modified Content :");
					System.out.println(file.getPatch());
				}
				System.out.println("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
