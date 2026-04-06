pipeline {
    agent {
        label "inbound-agent"
    }
    environment {
        FILE_NAME = 'OpenJDK25U-jdk_x64_linux_hotspot_25.0.2_10.tar.gz'
    }
    stages {
        stage('copy file to nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'nexus-auth', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    // withCredentials([string(credentialsId: 'nexus-auth-token', variable: 'NEXUS_TOKEN')]) {
                        sh '''
                        curl -sSLf -u "${NEXUS_USER}:${NEXUS_PASS}" \\
                        --upload-file "${TOOLSETPATH}/${FILE_NAME}" \\
                        http://192.168.183.100:8081/repository/raw/${FILE_NAME}

                        '''
                        // sh '''
                        // curl -sSLf -H "Authorization: Bearer ${NEXUS_TOKEN}" \
                        // --upload-file "${TOOLSETPATH}/${FILE_NAME}" \\
                        // http://192.168.183.100:8081/repository/raw/${FILE_NAME}
                        // '''
                        echo 'Uploaded successfully'
                    }
                }
            }
        }
    }
}