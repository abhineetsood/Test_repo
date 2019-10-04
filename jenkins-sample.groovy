#!/usr/bin/env groovy

//====================  Notes  ====================#
//  - Base pipeline for CCS team                   #
//  - Author: Hitesh Sharma                        #
//  - Notes:                                       #
//=================================================#

// Name of the Jenkins Node
node("xyz-node") {
    // Name of the Slack Channel where notifications will get sent
    def SLACK_CHANNEL = "#slack-channel-name"

    // This will enable timestamps on the jenkins console
    timestamps {
        try {
            // Define the name of the branch where slack library is located
            def SLACK_LIBRARY_BRANCH = "master"

            // Define the name of the directory where Github Repository will clone 
            def GIT_DIR = "dir-name"

            // Define the Github credentials stored on jenkins that will get used in this pipeline
            def GIT_CREDS = "abc-github"

            // Define your parameters
            properties([
                parameters([
                    choice(
                        name: "DECISION",
                        choices: ["DATA", "APP_DATA"],
                        description: "Select one of the options"),
                    string(
                        name: "GIT_BRANCH",
                        defaultValue: "master",
                        description: "Enter the name of your github branch"),
                    booleanParam(
                        name: "DRYRUN",
                        defaultValue: true,
                        description: "Please provide a description here"),
                ]),
                // Define the cronjob schedule
                pipelineTriggers([cron("* * * * *")])
            ])

            // This enables colors on the output console
            ansiColor("xterm") {
                // Slack Library [Update 'remote' with the correct git url]
                // NOTE: Dont forget to upload the library to github
                library identifier: "ccs@${SLACK_LIBRARY_BRANCH}", retriever: modernSCM([
                    $class: "GitSCMSource",
                    credentialsId: "${GIT_CREDS}",
                    remote: "https://github.cms.gov/XYZ.git",
                    ])

                // First Stage
                stage ("Stage 1: Clone Github Repository") {
                    deleteDir()
                    dir("${GIT_DIR}") {
                        git url: "https://github.cms.gov/blah-blah.git",
                        credentialsId: "${GIT_CREDS}",
                        branch: "${params.GIT_BRANCH}"
                    }
                } // stage

                // Second Stage
                stage ("stage 2") {
                    if ("${params.DECISION}" == "DATA") {
                        println("Deploying 'DATA' only because of the choice parameter ")
                    } else {
                        println("Deploying App and Data because of the choice parameter")
                    }
                }

                // third Stage
                stage("stage 3") {
                    if ("${params.DRYRUN}" == "true") {
                        // perform some action here if the parameter is set to True
                        println("building stage 3 because the parameter was set to true")
                    }
                } // stage

                // Fourth Stage
                stage("stage 4") {
                    echo "starting stage 4..."
                    input message: "Would you like to Continue to last stage?",
                    parameters: [
                        choice(
                            name: "Please select one option:",
                            choices: ["PROCEED", "ABORT"],
                            description: "Please enter your description here")
                    ]
                    println("building last stage because user selected PROCEED.")
                } // stage
            } // ansi color
        } catch (Exception e) {
            throw e
        } finally {
            slack(currentBuild.result, "${SLACK_CHANNEL}")
        }
    } // timestamp
} //node
