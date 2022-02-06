package helpers

def merge(String ramaOrigen, String ramaDestino) {

    withCredentials([gitUsernamePassword(credentialsId: env.GITHUB_CREDENTIALS_ID, gitToolName: 'Default')]) {
        sh "git fetch --all"

        checkout(ramaOrigen)
        checkout(ramaDestino)

        sh """
            git merge ${ramaOrigen}
            git push origin ${ramaDestino}
        """
    }
}

def tag(String rama) {
    withCredentials([gitUsernamePassword(credentialsId: env.GITHUB_CREDENTIALS_ID, gitToolName: 'Default')]) {
        sh "git fetch --all"
        checkout(rama)
        sh """
            git config user.name '${}'
            git config user.email 'my-ci-user@users.noreply.github.example.com'
            git tag -a ${rama} -m "add release ${rama}"
            git push origin --tags
        """
    }
}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout ${rama}; git pull origin ${rama}"
}

def release(String rama) {
    withCredentials([gitUsernamePassword(credentialsId: env.GITHUB_CREDENTIALS_ID, gitToolName: 'Default')]) {
        sh "git reset --hard HEAD; git checkout -b ${rama}; git push origin ${rama}"
    }
}

return this