
package com.gitrepositoryaccess.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

import fun.mike.dmp.Diff;
import fun.mike.dmp.DiffMatchPatch;

public class GitHubFileCompareWithPreviousVersion {
	public static void main(String[] args) throws IOException, ParseException {

		Properties props = new Properties();
		props.setProperty("login", "cr7blackpearl");
		props.setProperty("password", "Github@321");

		GitHub gitHub = GitHubBuilder.fromProperties(props).build();

		GHRepository repository = gitHub.getRepository("cr7blackpearl/Read-Git-Content-with-Basic-Auth");
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the Date :");
		String date = scanner.next();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date since = dateFormat.parse(date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(since);

		final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
		String timeInterval = "23:59";
		Date time = simpleDateFormat1.parse(timeInterval);
		cal.set(Calendar.HOUR_OF_DAY, time.getHours());
		cal.set(Calendar.MINUTE, time.getMinutes());
		Date until = cal.getTime();

		GHCommitQueryBuilder queryBuilder = repository.queryCommits().since(since).until(until);
		PagedIterable<GHCommit> commits = queryBuilder.list();
		Iterator<GHCommit> iterator = commits.iterator();

		while (iterator.hasNext()) {
			GHCommit commit = iterator.next();
			System.out.println("Commit: " + commit.getSHA1() + ", info: " + commit.getCommitShortInfo().getMessage()
					+ ", author: " + commit.getAuthor());
			System.out.println("File Name :" + commit.getFiles());
			List<String> parentSHA1s = commit.getParentSHA1s();
			System.out.println("Parent SHA :" + parentSHA1s);
			GHCompare compare = repository.getCompare(parentSHA1s.get(0), commit.getSHA1());
			URL diffUrl = compare.getDiffUrl();

			HttpURLConnection httpcon = (HttpURLConnection) new URL(diffUrl.toString()).openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

			// Read line by line
			StringBuilder responseSB = new StringBuilder();
			String line,plusLine;
			StringBuilder deletedLines = new StringBuilder();
			StringBuilder addedLines = new StringBuilder();

			while ((line = in.readLine()) != null) {
				responseSB.append("\n" + line);
				if (line.startsWith("-") || line.startsWith("+")) {
					if (line.startsWith("-")) {
						deletedLines.append("\n" + line);
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
								deletedLines.setLength(0);
								addedLines.setLength(0);
								deletedLines.append("\n" + plusLine);
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
}
