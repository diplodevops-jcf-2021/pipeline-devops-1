
void call(String buildTool = 'maven') {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = ''
            ARTIFACT_ID = ''
            ARTIFACT_NAME = ''
            ARTIFACT_GROUP_ID = ''
            ARTIFACT_VERSION = ''
        }
        stages {
            stage('pipeline') {
                steps {
                    script {

                        // Validar formato de nombre de rama release

                        if (buildTool == 'maven') {
                            maven.call(getPipelineType())
                        } else {
                            gradle.call(getPipelineType())
                        }
                    }
                }
            }
        }
        post {
            success {
                slackSend color: 'good', message: "[Secc2-Grp4][Pipeline: ${buildTool}][Rama: ${env.BRANCH_NAME}][Stage:${CURRENT_STAGE}] Ejecucion exitosa."
            }
            failure {
                slackSend color: 'danger', message: "[Secc2-Grp4][Pipeline: ${buildTool}][Rama: ${env.BRANCH_NAME}] Ejecucion fallida en stage [${CURRENT_STAGE}]."
            }
        }
    }
}

// Obtención de versión en formato "v#-#-#" de rama release
// si no viene en formato definido lanza error
String getReleaseVersion(){
    if (env.GIT_BRANCH.contains('release')){
        version = ( env.GIT_BRANCH =~ /release-(v\d+\-\d+\-\d+)/)
        if(version.find()){
            return version.group(1)
        }
        else{
            throw new Exception('Formato rama release inválido: ' + env.GIT_BRANCH )
        }
    }
}

String getPipelineType() {
    if (env.GIT_BRANCH.contains('feature-'))
        return 'CI-Feature'
    else if (env.GIT_BRANCH.equals('develop'))
        return 'CI-Develop'
    else if (env.GIT_BRANCH.contains('release-'))
        return 'CD'
}

return this
