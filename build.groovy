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
          git branch: 'main', url: 'git@github.com:vamsi8977/maven_sample.git'
      }
    }
stage('build') {
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
  }//end stages
post {
      success {
          archiveArtifacts artifacts: "target/*.jar"
      }
      failure {
          echo "The build failed."
      }
      cleanup{
        deleteDir()
      }
    }
}