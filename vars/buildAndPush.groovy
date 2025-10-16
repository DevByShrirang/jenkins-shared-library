def call(Map config = [:]) {
    // 1️⃣ Determine service name dynamically
    def service = config.SERVICE_NAME ?: env.SERVICE_NAME
    if (!service) { error "SERVICE_NAME is not defined!" }

    def region = config.REGION ?: 'us-east-2'
    def accountId = config.ACCOUNT_ID ?: '442042505508'

    stage("Docker Build & Push for ${service}") {
        dir("src/${service}") {   // Dockerfile should exist in src/<service>
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

