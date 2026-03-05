#!/usr/bin/env groovy

def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kind'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Jenkins CI'
    def gitUserEmail = config.gitUserEmail ?: 'jenkins@example.com'
    def gitBranch = config.gitBranch ?: 'kind'

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        sh """
        git config user.name "${gitUserName}"
        git config user.email "${gitUserEmail}"

        sed -i "s|image: rupali215/gemini-clone-app:.*|image: rupali215/gemini-clone-app:${imageTag}|g" ${manifestsPath}/gemini-deployment.yml

        if [ -f "${manifestsPath}/gemini-ingress.yml" ]; then
            sed -i "s|host: .*|host: localhost.nip.io|g" ${manifestsPath}/gemini-ingress.yml
        fi

        if git diff --quiet; then
            echo "No changes to commit"
        else
            git add ${manifestsPath}/*.yml
            git commit -m "Update image tags to ${imageTag} and ensure correct domain [ci skip]"

            git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/Rupali-1003/dev-gemini-clone.git

            git push origin HEAD:${gitBranch}
        fi
        """
    }
}
