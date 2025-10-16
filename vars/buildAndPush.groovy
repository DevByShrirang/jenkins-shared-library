def call(Map config) {
    def service = config.service
    def region = config.region ?: 'us-east-2'
    def accountId = config.accountId ?: '442042505508'

    stage("Docker Build & Push for ${service}") {
        dir("src/${service}") {
            sh '''
                docker system prune -f
                docker container prune -f
            '''
            sh "docker build -t ${service} ."
            sh """
                aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${accountId}.dkr.ecr.${region}.amazonaws.com
                docker tag ${service}:latest ${accountId}.dkr.ecr.${region}.amazonaws.com/${service}:${env.BUILD_NUMBER}
                docker push ${accountId}.dkr.ecr.${region}.amazonaws.com/${service}:${env.BUILD_NUMBER}
            """
        }
    }
}

