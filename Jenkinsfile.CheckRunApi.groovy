pipeline {
    agent { label 'linux' }
    environment {
        GITHUB_APP_ID = '3262961' // 替换为你的 App ID
        GITHUB_ACCOUNT = 'mymakingfun' // 替换为你的 GitHub 用户名或组织名
        GITHUB_REPO = 'jenkins-snippet' // 替换为你的仓库名
        GITHUB_API_URL = 'https://api.github.com'
        CREDENTIALS_ID = 'myapp-makingfun-secret' // 替换为你的 Jenkins 文件凭据ID
    }
    stages {
        stage('GitHub App API Access (Auto Installation ID)') {
            steps {
                script {

                    withCredentials([file(credentialsId: env.CREDENTIALS_ID, variable: 'GH_APP_KEY')]) {
                        // 1. 生成 JWT
                        def now = (System.currentTimeMillis() / 1000) as int
                        def exp = now + 600
                        def header = '{"alg":"RS256","typ":"JWT"}'
                        def payload = "{\"iat\":${now},\"exp\":${exp},\"iss\":${env.GITHUB_APP_ID}}"

                        def header_b64 = sh(script: "echo -n '${header}' | base64 -w 0 | tr '+/' '-_' | tr -d '='", returnStdout: true).trim()
                        def payload_b64 = sh(script: "echo -n '${payload}' | base64 -w 0 | tr '+/' '-_' | tr -d '='", returnStdout: true).trim()
                        def unsigned_token = "${header_b64}.${payload_b64}"

                        // 用 openssl 签名
                        // writeFile file: 'unsigned_token.txt', text: unsigned_token
                        // def signature_b64 = sh(
                        //     script: "openssl dgst -sha256 -sign ${GH_APP_KEY} unsigned_token.txt | openssl base64 -A | tr '+/' '-_' | tr -d '='",
                        //     returnStdout: true
                        // ).trim()
                        def signature_b64 = sh(
                            script: "echo -n '${unsigned_token}' | openssl dgst -sha256 -sign ${GH_APP_KEY} | base64 -w 0 | tr '+/' '-_' | tr -d '='",
                            returnStdout: true
                        ).trim()
                        def jwt = "${unsigned_token}.${signature_b64}"

                        // 2. 查询 installation id
                        def installationId = sh(
                            script: """
                                curl -s -H "Authorization: Bearer ${jwt}" \\
                                    -H "Accept: application/vnd.github+json" \\
                                    ${env.GITHUB_API_URL}/app/installations \\
                                    | jq -r '.[] | select(.account.login == "${env.GITHUB_ACCOUNT}") | .id'
                            """,
                            returnStdout: true
                        ).trim()
                        // def installations = new groovy.json.JsonSlurper().parseText(installationsJson)
                        // def installationId = installations.find { it.account.login == env.GITHUB_ACCOUNT }?.id
                        if (!installationId) {
                            error "No installation found for account: ${env.GITHUB_ACCOUNT}"
                        }
                        echo "Installation ID: ${installationId}"

                        // 3. 换取 Installation Token
                        def access_token = sh(
                            script: """
                                curl -sSL -X POST \\
                                  -H "Authorization: Bearer ${jwt}" \\
                                  -H "Accept: application/vnd.github+json" \\
                                  ${env.GITHUB_API_URL}/app/installations/${installationId}/access_tokens \\
                                  | jq -r '.token'
                            """,
                            returnStdout: true
                        ).trim()
                        // def access_token = new groovy.json.JsonSlurper().parseText(tokenJson).token

                        // 4. 用 Installation Token 访问 GitHub API
                        // def repoJson = sh(
                        //     script: """
                        //         curl -sSL -H "Authorization: Bearer ${access_token}" \\
                        //             -H "Accept: application/vnd.github+json" \\
                        //             ${env.GITHUB_API_URL}/repos/${env.GITHUB_ACCOUNT}/${env.GITHUB_REPO}
                        //     """,
                        //     returnStdout: true
                        // ).trim()
                        // echo "Repo Info: ${repoJson}"

                         def check_url="${GITHUB_API_URL}/repos/${GITHUB_ACCOUNT}/${GITHUB_REPO}/check-runs"
                         def commit="7cf911f745fd0c0b367109d04e285e8b3ce0b635"
                         def checkRunJson = sh(
                            script: """
                                curl -sSL -X POST ${check_url} \\
                                -H "Authorization: Bearer ${access_token}" \\
                                -H "Accept: application/vnd.github.v3+json" \\
                                -d "{
                                    \\"name\\": \\"ci/build\\",
                                    \\"head_sha\\": \\"${commit}\\",
                                    \\"status\\": \\"completed\\",
                                    \\"details_url\\": \\"http://jenkins.example.com\\",
                                    \\"external_id\\": \\"jenkins\\",
                                    \\"conclusion\\": \\"success\\",
                                    \\"output\\": {
                                        \\"title\\": \\"Build in progress\\",
                                        \\"summary\\": \\"The build is currently running.\\",
                                        \\"text\\": \\"Check the Jenkins console for more details.\\"
                                    }
                                }"
                            """,
                            returnStdout: true
                         )
                         echo "Check Run Response: ${checkRunJson}"
                    }
                }
            }
        }
    }
}