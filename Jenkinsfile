pipeline {
    agent {
        docker {
            image 'maven:3.9.14-eclipse-temurin-25'
            args '-u 111:113 -v /var/lib/jenkins/.m2:/var/lib/jenkins/.m2'
        }
    }
    environment {
        PROJECT_NAME = 'java-project'
    }
    stages {
        stage('Checkout') {
            steps {
                sh "rm -rf ${PROJECT_NAME}"
                withCredentials([usernamePassword(credentialsId: 'github-https', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                        git clone --branch main https://$GIT_USER:$GIT_TOKEN@github.com/mymakingfun/${PROJECT_NAME}.git ${PROJECT_NAME}
                    """
                }
            }
        }
        stage('Build with Maven') {
            steps {
                dir("${PROJECT_NAME}") {
                    sh 'mvn clean package -Dmaven.repo.local=/var/lib/jenkins/.m2/repository'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
    }
}