/**
 * Copyright (C) 2016 Matthieu Brouillard [http://oss.brouillard.fr/jgitver] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.brouillard.oss.jgitver.strategy.maven.others;

import static fr.brouillard.oss.jgitver.Lambdas.mute;
import static fr.brouillard.oss.jgitver.Lambdas.unchecked;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.brouillard.oss.jgitver.BranchingPolicy;
import fr.brouillard.oss.jgitver.BranchingPolicy.BranchNameTransformations;
import fr.brouillard.oss.jgitver.GitVersionCalculator;
import fr.brouillard.oss.jgitver.Misc;
import fr.brouillard.oss.jgitver.Scenarios;
import fr.brouillard.oss.jgitver.Scenarios.Scenario;

public class Scenario13GitflowWithNonQualifierAndPartialNameTest {
    private static Scenario scenario;
    private Repository repository;
    private Git git;
    private GitVersionCalculator versionCalculator;

    /**
     * Initialiaze the whole junit class tests ; creates the git scenario.
     */
    @BeforeClass
    public static void initClass() {
        scenario = Scenarios.s13_gitflow();
        if (Misc.isDebugMode()) {
            System.out.println("git repository created under: " + scenario.getRepositoryLocation());
        }
    }

    /**
     * Cleanup the whole junit scenario ; deletes the created git repository.
     */
    @AfterClass
    public static void cleanupClass() {
        try {
            Misc.deleteDirectorySimple(scenario.getRepositoryLocation());
        } catch (Exception ignore) {
            System.err.println("cannot remove " + scenario.getRepositoryLocation());
        }
    }

    /**
     * Prepare common variables to access the git repository.
     * 
     * @throws IOException if a disk error occurred
     */
    @Before
    public void init() throws IOException {
        repository = new FileRepositoryBuilder().setGitDir(scenario.getRepositoryLocation()).build();
        git = new Git(repository);
        versionCalculator = GitVersionCalculator
                .location(scenario.getRepositoryLocation())
                .setMavenLike(true)
                .setQualifierBranchingPolicies(
                    BranchingPolicy.ignoreBranchName("master"),
                    BranchingPolicy.fixedBranchName("develop"),
                    new BranchingPolicy("release/(.*)", Collections.singletonList(BranchNameTransformations.IGNORE.name())),
                    new BranchingPolicy("feature/(.*)", Arrays.asList(
                            BranchNameTransformations.REMOVE_UNEXPECTED_CHARS.name(),
                            BranchNameTransformations.LOWERCASE_EN.name())
                    )
                )
                .setUseDefaultBranchingPolicy(false);

        // reset the head to master
        unchecked(() -> git.checkout().setName("master").call());
    }

    /**
     * Cleanups after each tests.
     */
    @After
    public void clean() {
        mute(() -> git.close());
        mute(() -> repository.close());
        mute(() -> versionCalculator.close());
    }

    @Test
    public void head_is_on_master_by_default() throws Exception {
        assertThat(repository.getBranch(), is("master"));
    }
    
    @Test
    public void version_of_master() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("master").call());
        assertThat(versionCalculator.getVersion(), is("3.0.0"));
    }
    
    @Test
    public void version_of_branch_release_1x() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("release/1.x").call());
        assertThat(versionCalculator.getVersion(), is("1.0.1-SNAPSHOT"));
    }
    
    @Test
    public void version_of_branch_release_2x() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("release/2.x").call());
        assertThat(versionCalculator.getVersion(), is("2.0.0-SNAPSHOT"));
    }
    
    @Test
    public void version_of_branch_develop() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("develop").call());
        assertThat(versionCalculator.getVersion(), is("1.0.1-develop-SNAPSHOT"));
    }
    
    @Test
    public void version_of_a_feature_branch() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("feature/add-sso").call());
        assertThat(versionCalculator.getVersion(), is("1.0.1-addsso-SNAPSHOT"));
    }
}
