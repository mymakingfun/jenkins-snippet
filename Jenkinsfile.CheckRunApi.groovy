pipeline {
    agent none
    environment {
        GITHUB_API_URL = 'https://api.github.com'
        GITHUB_REPO_OWNER = 'mymakingfun'
    }
    stages {
        stage('call github api check') {
            agent {
                docker {
                    image 'devopsinfra/docker-terragrunt:latest'
                    args '-e http_proxy=http://192.168.183.100:8118 -e https_proxy=http://192.168.183.100:8118'
                }
            }
            steps {
                script {
                    def repo="jenkins-snippet"
                    def commit="7cf911f745fd0c0b367109d04e285e8b3ce0b635"

                    def check_url="${GITHUB_API_URL}/repos/${GITHUB_REPO_OWNER}/${repo}/check-runs"


                    withCredentials([[$class: 'GitHubAppCredentialsBinding', credentialsId: 'myapp-makingfun', accessTokenVariable: 'GITHUB_TOKEN']]) {
                        sh """
                            curl -sSL -X POST "${check_url}" \
                            -H "Authorization: Bearer ${GITHUB_TOKEN}" \
                            -H "Accept: application/vnd.github.v3+json" \
                            -d "{
                            "name": "ci/build",
                            "head_sha": "${commit}",
                            "status": "in_progress",
                            "details_url": "http://jenkins.example.com",
                            "external_id": "jenkins",
                            "conclusion": null,
                            "output": {
                                "title": "Build in progress",
                                "summary": "The build is currently running.",
                                "text": "Check the Jenkins console for more details."
                              }
                            }"
                        """
                    }
                }
                
            }
        }
    }
}