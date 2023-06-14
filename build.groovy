#!/usr/bin/env groovy
@Library('shared-library') _

pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
    }
  stages {
    stage('CheckOut') {
      steps {
        echo 'Checking out project from Bitbucket....'
        cleanWs()
        checkout([
          $class: 'GitSCM',
          branches: [[name: 'main']],
          userRemoteConfigs: [[url: 'git@github.com:vamsi8977/maven_sample.git']]
        ])
      }
    }
    stage('Build') {
      steps {
        script {
          withSonarQubeEnv('SonarQube') {
            maven()
          }
        }
      }
    }
    stage('OWASP') {
      steps {
        script {
          def dependencyCheckArgs = [
            '-o', './',
            '-s', './',
            '-f', 'ALL',
            '--prettyPrint'
          ].join(' ')
          def dependencyCheckInstallation = tool 'OWASP Dependency-Check'
          withEnv(["PATH+OWASPDependencyCheck=${dependencyCheckInstallation}/bin"]) {
            sh "dependency-check.sh ${dependencyCheckArgs}"
          }
          dependencyCheckPublisher pattern: 'dependency-check-report.xml'
        }
      }
    }
  }
  post {
    success {
      echo "The build passed."
    }
    failure {
      echo "The build failed."
    }
    cleanup {
      deleteDir()
    }
  }
}
