// config file provider
pipeline {
    agent {
        label "inbound-agent"
    }
    environment {
        FILE_NAME = 'OpenJDK25U-jdk_x64_linux_hotspot_25.0.2_10.tar.gz'
        GROUP_ID = 'com.example'
        ARTIFACT_ID = 'openjdk'
        VERSION = '25.0.2-10'
        SNAPSHOT_VERSION = '25.0.2-10-SNAPSHOT'
        PACKAGING = 'tar.gz'
    }
    stages {
        // stage('maven update to nexus snapshots') {
        //     steps {
        //         script {
        //             configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS')]) {
        //                 sh '''
        //                 mvn deploy:deploy-file \
        //                 -DgroupId=${GROUP_ID} \
        //                 -DartifactId=${ARTIFACT_ID} \
        //                 -Dversion=${SNAPSHOT_VERSION} \
        //                 -Dpackaging=${PACKAGING} \
        //                 -Dfile=${TOOLSETPATH}/${FILE_NAME} \
        //                 -DrepositoryId=maven-snapshots \
        //                 -Durl=http://192.168.183.100:8081/repository/maven-snapshots/ \
        //                 --settings ${MAVEN_SETTINGS}
        //                 '''
        //             }    
        //         }
        //         echo 'updated successfully'
        //     }
        // }

        // stage('maven update to nexus release') {
        //     steps {
        //         script {
        //             configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS')]) {
        //                 sh '''
        //                 mvn deploy:deploy-file \
        //                 -DgroupId=${GROUP_ID} \
        //                 -DartifactId=${ARTIFACT_ID} \
        //                 -Dversion=${VERSION} \
        //                 -Dpackaging=${PACKAGING} \
        //                 -Dfile=${TOOLSETPATH}/${FILE_NAME} \
        //                 -DrepositoryId=maven-releases \
        //                 -Durl=http://192.168.183.100:8081/repository/maven-releases/ \
        //                 --settings ${MAVEN_SETTINGS}
        //                 '''
        //             }    
        //         }
        //         echo 'updated successfully'
        //     }
        // }

        stage('maven update to nexus release with classifier') {
            steps {
                script {
                    def getMavenPath = {
                        groupId, artifactId, version, packaging, classifier -> 
                        def groupPath = groupId.replace('.', '/')
                        def fileName = classifier ?
                            "${artifactId}-${version}-${classifier}.${packaging}" : 
                             "${artifactId}-${version}.${packaging}"
                        return "${groupPath}/${artifactId}/${version}/${fileName}"
                    }

                    configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS')]) {
                        def artifactPath = getMavenPath(
                            env.GROUP_ID, "${ARTIFACT_ID}",
                            env.VERSION, env.PACKAGING,
                            'linux')
                        
                        def artifactUrl = "http://192.168.183.100:8081/repository/maven-releases/${artifactPath}"
                        echo "${artifactUrl}"

                        def exists = false
                        withCredentials([usernamePassword(credentialsId: 'nexus-auth', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                            withEnv(["ARTIFACT_URL=${artifactUrl}"]) {
                                exists = sh(
                                    script: 'curl -sfI -u ${NEXUS_USER}:${NEXUS_PASS} ${ARTIFACT_URL} > /dev/null',
                                    returnStatus: true
                                ) == 0
                            }

                        }

                        
                        if (exists) {
                            echo "Artifact already exists in nexus, skip upload"
                        } else {
                        sh '''
                            mvn deploy:deploy-file \
                            -DgroupId=${GROUP_ID} \
                            -DartifactId=${ARTIFACT_ID} \
                            -Dversion=${VERSION} \
                            -Dpackaging=${PACKAGING} \
                            -Dfile=${TOOLSETPATH}/${FILE_NAME} \
                            -DrepositoryId=maven-releases \
                            -Durl=http://192.168.183.100:8081/repository/maven-releases/ \
                            -Dclassifier=linux \
                            -DgeneratePom=false \
                            --settings ${MAVEN_SETTINGS}
                            '''                            
                        }

                    }    
                }
                echo 'updated successfully'
            }
        }
    }
}