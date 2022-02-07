
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

                        String pipelineType = getPipelineType()

                        if (buildTool == 'maven') {
                            maven.call(pipelineType)
                        } else {
                            gradle.call(pipelineType)
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

// Si no cumple con el patrón de ninguna de las 3 opciones, lanzará error
String getPipelineType(){
    if (env.GIT_LOCAL_BRANCH ==~ /^release-v(\d+)-(\d+)-(\d+)$/){
        return 'CD'
    } else if (env.GIT_LOCAL_BRANCH ==~ /^develop$/) {
        return 'CI-Develop'
    } else if (env.GIT_LOCAL_BRANCH ==~ /^feature-(.+)$/) {
        return 'CI-Feature'
    } else {
        throw new Exception('Formato rama inválido: ' + env.GIT_LOCAL_BRANCH )
    }
}

return this
