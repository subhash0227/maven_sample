pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
    }
  stages {
    stage('SCM') {
      steps {
        cleanWs()
        echo 'Checking out project from Bitbucket....'
        git branch: 'main', url: 'git@github.com:vamsi8977/maven_sample.git'
      }
    }
    stage('Build') {
      steps {
        ansiColor('xterm') {
          echo 'Maven Build....'
          sh "mvn clean install"
        }
      }
    }
    stage('SonarQube') {
      steps {
        withSonarQubeEnv('SonarQube') {
          sh "mvn sonar:sonar -Dsonar.projectKey=maven -Dsonar.projectName='maven_sample'"
        }
      }
    }
    stage('JFrog') {
      steps {
        ansiColor('xterm') {
          sh '''
            jf rt u target/javaparser-maven-sample-1.0-SNAPSHOT.jar maven/
            jf scan target/*.jar --fail-no-op --build-name=maven --build-number=$BUILD_NUMBER
          '''
        }
      }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: "target/*.jar"
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
