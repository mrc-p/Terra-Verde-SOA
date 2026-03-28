1. Instruções de Execução
Na sequência, estão apresentados todos os passos para executar o sistema no computador com Eclise IDLE  e a consumi-lo no Postman.


1.1  Requisitos de Ambiente
Para rodar o projeto, você precisa ter instalado:

Eclipse IDE: Recomendado o pacote Eclipse IDE for Enterprise Java and Web Developers;

Java Development Kit (JDK): Versão 17 ou superior configurada no Eclipse;

Apache Maven: O Eclipse deve estar configurado para usar o Maven (geralmente já vem integrado);

PostgreSQL Server: Versão 10 ou superior.

1.2 Importação do Projeto no Eclipse
Baixe o projeto do repositório em Code > Download ZIP neste link;


Descompacte o arquivo baixado;


No Eclipse, vá em File > Import....;


Selecione Maven > Existing Maven Projects;


Clique em Browse... e selecione o diretório raiz do projeto (onde está o arquivo pom.xml);


Clique em Finish. O Eclipse irá importar o projeto e baixar as dependências do Maven.

1.3 Atualização de dependências do Maven
Clique com o botão direito em cima da pasta do projeto;


Selecione Maven > Update Project > marque Force update of snapshots/releases > Ok;


Agora vá em Window, na barra de opções superior do Eclipse, clique em Preferences > Maven;


Tenha marcado as caixinhas Download Artifact Sources, Download Artifact Javadoc, Download repository index updates on startup, Update Maven projects on startup e Automatically update Maven projects configuration.


1.4 Instalação do Plugin Lombok no Eclipse
O Lombok usa um truque para modificar o código-fonte compilado (bytecode) durante o build. O Eclipse precisa de um plugin instalado em sua JVM para permitir essa alteração.
7.4.1 Localizando o Arquivo JAR
O arquivo .jar do Lombok é baixado pelo Maven para o seu repositório local. Siga os passos:
No Eclipse, localize o JAR do Lombok no seu projeto (geralmente em Maven Dependencies);


Clique com o botão direito no arquivo JAR e vá em Properties;


Copie o caminho completo (Path) para o arquivo, que geralmente é similar a: C:\Users\seu_usuario\.m2\repository\org\projectlombok\lombok\1.18.30\lombok-1.18.32.jar
7.4.2 Execução do Instalador
Abra o terminal ou prompt de comando;


Execute o arquivo JAR do Lombok (use o caminho completo que você copiou acima);


java -jar C:\Users\seu_usuario\.m2\repository\org\projectlombok\lombok\1.18.30\lombok-1.18.32.jar
Uma janela de instalação do Lombok aparecerá e o instalador tentará detectar automaticamente as instalações do Eclipse.
Se o Eclipse não for detectado, clique em Specify location…, procure e selecione o arquivo executável do Eclipse (o eclipse.exe).


Clique em Install / Update;


Após a instalação, feche completamente o Eclipse;ç


Reinicie o Eclipse.


1.5  Configuração do Banco de Dados (PostgreSQL)
O projeto exige uma instância do PostgreSQL rodando no computador e um banco de dados configurado. Então baixe e instale o PostgreSQL na sua máquina. Depois, você pode usar a interface pgAdmin ou o psql (terminal) para criar o banco de dados:


1.5.1 Comando SQL para criar o Banco de Dados:

CREATE DATABASE db_terra_verde


1.5.2 Configuração do Arquivo application.properties

Abra o arquivo src/main/resources/application.properties e ajuste as credenciais de conexão do seu banco de dados:

spring.datasource.url=jdbc:postgresql://localhost:5432/db_terra_verde
spring.datasource.username=seu_usuario_postgres
spring.datasource.password=sua_senha_postgres
spring.datasource.driver-class-name=org.postgresql.Driver


1.5.3 Configuração do hibernate

Tenha certeza que o hibernate esteja configurado assim:

spring.jpa.hibernate.ddl-auto=update  # Cria ou atualiza as tabelas automaticamente
spring.jpa.show-sql=true

1.6  Execução do Projeto no Eclipse
Após após configurar o banco de dados, resta executar  a aplicação:

Clique na pasta do projeto;


Localize um ícone de  “play” verde com uma seta do lado, na barra superior de opções do Eclipse;


Clique na seta e selecione “TerraVerdeSoaApplication” para rodar a aplicação;


O console do Eclipse exibirá as informações de inicialização. Procure pela mensagem indicando que o servidor foi iniciado na porta 8080:
>>> Usuário ADMIN inicial criado: admin/admin123
... Started CadastroClienteFornecedorApplication in X.XXX seconds (process running for Y.YYY)

7.6.1 Primeiro Acesso e Credenciais


Abra seu navegador e acesse a URL: http://localhost:8080/;


O sistema irá redirecionar para a página de Login;


Credenciais Iniciais para testar (Injetadas pelo SecurityConfig.java):
Username: admin
            Password: admin123


Para confirmar que a aplicação está no ar, você pode acessar o endereço  do WSDL no navegador em “http://localhost:8080/ws/terraverdesoa.wsdl”.


1.7  Passos para Consumir o Serviço via Postman


O sistema foi testado no Postman, mas também pode ser executado em outras interfaces. 



1.7.1 Configurar a requisição no Postman:


Selecione o método POST;


Cole essa URL: http://localhost:8080/ws;


Na aba Headers  adicione Content-Type = text/xml;


Na aba Body, selecione o raw e escolha o formato XML;




Copie o XML de login da seção 4.1, cole no body e envie. Copie o valor de <mensagem> da resposta. Esse é o seu token;


Inclua o token no <soapenv:Header> de todas as requisições protegidas, conforme o exemplo:
 <soapenv:Header>
<tns:token>COLE_SEU_TOKEN_AQUI</tns:token>
</soapenv:Header>
