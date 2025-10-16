def call(Map config = [:]) {
    def service = config.SERVICE_NAME ?: env.SERVICE_NAME
    if (!service) { error "SERVICE_NAME is not defined!" }

    def k8sDir = 'kubernetes-files'
    if (!fileExists(k8sDir)) { error "${k8sDir} does not exist!" }

    def yamlFile = config.YAML_FILE ?: "${service}.yaml"
    def repoUrl = config.REPO_URL ?: "${config.ACCOUNT_ID ?: '442042505508'}.dkr.ecr.us-east-2.amazonaws.com/${service}"
    def gitEmail = config.GIT_EMAIL ?: "shrirang.patil1812@gmail.com"
    def gitUser = config.GIT_USER ?: "DevByShrirang"
    def gitRepo = config.GIT_REPO ?: "Microservices-E-Commerce-eks-project01"
    def gitTokenId = config.GIT_TOKEN_ID ?: 'my-git-pattoken'

    stage("Update Manifest for ${service}") {
        dir(k8sDir) {
            withCredentials([string(credentialsId: gitTokenId, variable: 'git_token')]) {
                sh """
                    git config user.email "${gitEmail}"
                    git config user.name "${gitUser}"
                    sed -i "s#image:.*#image: ${repoUrl}:${env.BUILD_NUMBER}#g" ${yamlFile}
                    git add ${yamlFile}
                    git commit -m "Update ${service} image to version ${env.BUILD_NUMBER}" || echo "No changes to commit"
                    git push https://${git_token}@github.com/${gitUser}/${gitRepo} HEAD:master
                """
            }
        }
    }
}

