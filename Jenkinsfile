pipeline {
    agent {
        docker {
            image 'maven:3.9.14-eclipse-temurin-25'
            args '-v /var/lib/jenkins/.m2:/root/.m2'
        }
    }
    environment {
        PROJECT_NAME = 'java-project'
    }
    stages {
        stage('Checkout') {
            steps {
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
                    sh 'mvn clean package'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
    }
}