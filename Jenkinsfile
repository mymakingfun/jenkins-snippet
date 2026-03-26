pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'git@github.com:mymakingfun/jenkins-snippet.git',
                        credentialsId: 'github-ssh'
                    ]],
                    doGenerateSubmoduleConfigurations: false,
                    submoduleCfg: [],
                    extensions: []
                ])
            }
        }
        stage('Build with Maven') {
            steps {
                withMaven() {
                    sh 'mvn clean package'
                }
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                junit '**/target/surefire-reports/*.xml'
            }
        }
    }
}