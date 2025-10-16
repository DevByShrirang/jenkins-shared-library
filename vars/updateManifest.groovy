def call(Map config) {
    def service = config.service
    def repoUrl = config.repoUrl
    def yamlFile = config.yamlFile
    def gitEmail = config.gitEmail
    def gitUser = config.gitUser
    def gitRepo = config.gitRepo
    def gitTokenId = config.gitTokenId ?: 'my-git-pattoken'

    stage("Update Manifest for ${service}") {
        dir('kubernetes-files') {
            withCredentials([string(credentialsId: gitTokenId, variable: 'git_token')]) {
                sh """
                    git config user.email "${gitEmail}"
                    git config user.name "${gitUser}"

                    sed -i "s#image:.*#image: ${repoUrl}:${env.BUILD_NUMBER}#g" ${yamlFile}
                    git add .
                    git commit -m "Update ${service} image to version ${env.BUILD_NUMBER}"
                    git push https://${git_token}@github.com/${gitUser}/${gitRepo} HEAD:master
                """
            }
        }
    }
}

