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
                        url: 'git@github.com:mymakingfun/java-project.git',
                        credentialsId: 'github-ssh'
                    ]],
                    doGenerateSubmoduleConfigurations: false,
                    submoduleCfg: [],
                    extensions: [[
                        $class: 'RelativeTargetDirectory',
                        relativeTargetDir: 'java-project'
                    ]]
                ])
            }
        }
        stage('Build with Maven') {
            steps {
                dir('java-project') {
                    withMaven() {
                        sh 'mvn clean package'
                    }
                }
                archiveArtifacts artifacts: 'java-project/target/*.jar', fingerprint: true
                junit 'java-project/target/surefire-reports/*.xml'
            }
        }
    }
}