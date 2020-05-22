package com.gitrepositoryaccess;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InvalidRemoteException, TransportException, GitAPIException
    {
        System.out.println( "Hello World!" );
        
        Repository createNewRepo=FileRepositoryBuilder.create(new File("C://Users//rohit.patil09//git//git-access///.git"));
        
        System.out.println(createNewRepo);
        
        Repository existingRepo=new FileRepositoryBuilder().setGitDir(new File("/home/rohit/git/TestProject/.git/config")).build();
        System.out.println(existingRepo.getBranch());
        
        CheckoutCommand checkout = Git.open(new File("/home/rohit/git/simpleprograms")).checkout();
        Repository repository=checkout.getRepository();
        System.out.println(repository);
        
        String branch = repository.getBranch();
        System.out.println(branch);
        
        Git git = Git.cloneRepository()
        	.setURI("https://github.com/cr7blackpearl/TestProject.git")
        	.setDirectory(new File("/home/rohit/git/demojtog")).call();
        System.out.println(git);
        
       
    }
    
}
