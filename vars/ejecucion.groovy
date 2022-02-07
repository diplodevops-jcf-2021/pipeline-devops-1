
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
            String pipeline = getPipeline()

            success {
                slackSend color: 'good', message: "[Grupo4][Pipeline ${pipeline}][Rama: ${env.GIT_LOCAL_BRANCH}][Stage:${CURRENT_STAGE}][Resultado: Ok]"
            }
            failure {
                slackSend color: 'danger', message: "[Grupo4][Pipeline ${pipeline}][Rama: ${env.GIT_LOCAL_BRANCH}][Stage:${CURRENT_STAGE}][Resultado: No Ok]."
            }
        }
    }
}

String getPipeline() {
    String pipelineType = getPipelineType()
    String pipeline = ''
    if (pipelineType.contains('CI')) {
        String pipeline = 'IC'
    } else if (pipelineType.contains('CD')) {
        String pipeline = 'Release'
    }

    return pipeline
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
