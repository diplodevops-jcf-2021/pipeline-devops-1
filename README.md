# pipeline-devops

Jenkins debe tener configuradas las siguientes variables de entorno:

- `GITHUB_CREDENTIALS_ID`: credenciales de github configurados en jenkins
- `NEXUS_INSTANCE_ID`: id de la instancia de nexus configurada en jenkins
- `NEXUS_REPOSITORY_ID`: nombre del repositorio de nexus
- `NEXUS_CREDENTIALS_ID`: credenciales de nexus configuradas en jenkins
- `NEXUS_URL`: Url al repositorio de nexus
- `SONAR_QUBE_NAME`: nombre de la instalación de sonarqube en jenkins

Para configurar estas variables de entorno globales, deben ir a `http://<url-jenkins>/configure` sección `Propiedades Globales > Variables de Entorno`.