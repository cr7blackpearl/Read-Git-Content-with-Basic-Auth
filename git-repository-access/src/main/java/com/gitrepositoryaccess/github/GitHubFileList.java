package com.gitrepositoryaccess.github;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

public class GitHubFileList {
	public static void main(String[] args) throws IOException {

		Properties props = new Properties();
		props.setProperty("login", "cr7blackpearl");
		props.setProperty("password", "Github@321");

		GitHub gitHub = GitHubBuilder.fromProperties(props).build();

		GHRepository repository = gitHub.getRepository("cr7blackpearl/Read-Git-Content-with-Basic-Auth");

		Calendar cal = Calendar.getInstance();
		cal.set(2020, 4, 4);
		Date since = cal.getTime();
		cal.set(2020, 06, 15);
		Date until = cal.getTime();

		GHCommitQueryBuilder queryBuilder = repository.queryCommits().since(since).until(until);
		PagedIterable<GHCommit> commits = queryBuilder.list();
		Iterator<GHCommit> iterator = commits.iterator();

		while (iterator.hasNext()) {
			GHCommit commit = iterator.next();
			System.out.println("Commit: " + commit.getSHA1() + ", info: " + commit.getCommitShortInfo().getMessage()
					+ ", author: " + commit.getAuthor());
			GHCommit gitCommit = repository.getCommit(commit.getSHA1());
			List<File> files = gitCommit.getFiles();
			for (File file : files) {
				System.out.println("File Name: "+file.getFileName());
				
			}
			System.out.println("\n");
		}
	}
}
