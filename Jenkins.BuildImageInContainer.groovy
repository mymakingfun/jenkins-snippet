// buildah root + 特权模式
pipeline {
    agent none
    stages {
        stage('prepare docker file') {
            agent {label 'inbound-agent'}
            steps {
                script {
                    sh """
                    |cat >${WORKSPACE}/Dockerfile <<EOF
                    |FROM 192.168.183.100:8081/docker/alpine:latest
                    |RUN echo "hello"
                    |EOF
                    |""".stripMargin().trim()
                }
            }
        }

        stage('build image') {
            agent {label 'buildah'}
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'nexus-auth', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        sh "echo $HOME; mkdir -p ${HOME}/.docker"
                        def token = sh(script: "echo -n '${NEXUS_USER}:${NEXUS_PASS}' | base64 -w0", returnStdout: true)
                        
                        withEnv(["DOCKER_TOKEN=${token}"]) {
                            sh '''
                            |cat >${HOME}/.docker/config.json <<EOF
                            |{
                            |   "auths": {
                            |     "http://192.168.183.100:8081/v1/": {
                            |       "auth": "${DOCKER_TOKEN}"
                            |     }
                            |   }  
                            |}
                            |EOF
                            |'''.stripMargin().trim()
                        }

                    }

                    sh '''
                    |cat > /etc/containers/registries.conf <<EOF
                    |unqualified-search-registries = ["docker.io"]
                    |
                    |[[registry]]
                    |prefix = "192.168.183.100:8081"
                    |location = "192.168.183.100:8081"
                    |insecure = true
                    |EOF
                    |'''.stripMargin().trim()

                    sh 'ls -al'
                    sh 'id'
                    sh 'buildah bud -t 192.168.183.100:8081/docker/alpine:test .'
                    sh 'ls -al'
                    sh 'buildah push 192.168.183.100:8081/docker/alpine:test'
                    echo 'Built and pushed successfully'     
                }
            }
        }
    }
}