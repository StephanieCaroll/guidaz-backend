# 🚗 Guidaz

![GitHub repo size](https://img.shields.io/github/repo-size/StephanieCaroll/guidaz?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/StephanieCaroll/guidaz?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/StephanieCaroll/guidaz?style=for-the-badge)
![GitHub pull requests](https://img.shields.io/github/issues-pr/StephanieCaroll/guidaz?style=for-the-badge)
![Ionic](https://img.shields.io/badge/Ionic-3880FF?style=for-the-badge&logo=ionic&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)

> **A Plataforma Guidaz** é um ecossistema completo (Mobile + API REST) focado na centralização da gestão de instrutores autônomos de direção. O projeto foi desenvolvido com uma arquitetura moderna baseada em **Angular** e **Ionic** no front-end, e **Spring Boot** no back-end, garantindo uma jornada integrada de ponta a ponta.

## 📂 Repositórios do Projeto

O sistema da Guidaz é dividido em duas partes que funcionam em conjunto:

- 📱 Front-end (Mobile App): [Repositório Front-end (Guidaz)](https://github.com/StephanieCaroll/Guidaz)
- ⚙️ Back-end (API REST Spring Boot): [Repositório Back-end (guidaz-backend)](https://github.com/StephanieCaroll/guidaz-backend)

## ✨ Sobre o Projeto

O projeto resolve o problema de fragmentação na rotina dos instrutores, que utilizam várias ferramentas desconectadas (planilhas, WhatsApp, anotações). A Guidaz integra toda a jornada (contratação → agendamento → aula → registro → avaliação) em um único hub de gestão unificado.

### Principais Tecnologias
- **Front-end**: Angular, Ionic Framework, TypeScript, SCSS.
- **Back-end**: Java, Spring Boot, Spring Data JPA, Spring Security, Banco de Dados Relacional.
- **Arquitetura**: Componentização segmentada por perfis (Instrutor e Aluno) consumindo uma API RESTful robusta.

---

### 🚀 Funcionalidades do MVP
- ✅ **Gestão de Agenda**: Definição e bloqueio de horários disponíveis pelo instrutor.
- ✅ **Agendamento Autônomo**: Alunos podem visualizar horários livres e reservar aulas diretamente.
- ✅ **Controle Financeiro**: Contratação de pacotes e gestão de pagamento via Pix com validação manual.
- ✅ **Transparência Contábil**: Controle visual e detalhado do saldo de aulas (contratadas, realizadas e pendentes).
- ✅ **Feedback**: Sistema de avaliação pós-aula (1 a 5 estrelas) com métricas de didática e pontualidade.

---

## 💻 Pré-requisitos

Antes de começar, certifique-se de atender aos seguintes requisitos:
- **Node.js** (v22.22.3 ou superior) e **npm**.
- **Ionic CLI** instalado globalmente (`npm install -g @ionic/cli`).
- **Java Development Kit (JDK 17 ou superior)** para rodar o back-end.
- **Maven** (ou utilize o wrapper `./mvnw` incluso).
- **MySQL** instalado e rodando localmente (porta 3306). [-->Caso você não saiba como baixar clique aqui <--](https://www.youtube.com/watch?v=VJL-sQ6pe-M)
- Um editor de código (recomendado: **VS Code** para o front-end e **IntelliJ IDEA / Eclipse** para o back-end).

---

## 🚀 Como Executar o Projeto (Full-Stack)

Como o Front-end e o Back-end funcionam em conjunto, siga os passos abaixo para iniciar ambos os ambientes:

### 1. Configurando o Banco de Dados e o Back-end (API Spring Boot)

O back-end utiliza MySQL. Antes de rodar a aplicação, você precisa configurar o banco de dados e as variáveis de ambiente:

**Passo A: Criar o Banco de Dados**  
No seu MySQL (via Workbench ou terminal), execute o comando para criar o banco vazio:
```sql
CREATE DATABASE guidaz_db;
```

**Passo B: Configurar o arquivo `.env`**  
Na raiz da pasta do back-end (`guidaz-backend`), crie um arquivo chamado `.env` e adicione a senha do seu usuário root do MySQL:
```env
DB_PASSWORD=sua_senha_aqui
```
*(Atenção: Certifique-se de que o arquivo `.env` está listado no seu `.gitignore` para não subir para o repositório)*

**Passo C: Configurar o `application.properties`**  
Verifique se o arquivo `src/main/resources/application.properties` está com as seguintes configurações para ler o `.env` e gerenciar as tabelas automaticamente:
```properties
# Conexão com o MySQL Local
spring.datasource.url=jdbc:mysql://localhost:3306/guidaz_db?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configurações Críticas do Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**Passo D: Rodar o Back-end**  
Abra um terminal na pasta do repositório do back-end (`guidaz-backend`):

```bash
# No Linux/macOS
./mvnw spring-boot:run

# No Windows (CMD/PowerShell)
mvnw.cmd spring-boot:run
```
O servidor da API iniciará em `http://localhost:8080` e criará as tabelas necessárias no MySQL automaticamente.

### 2. Configurando e Rodando o Front-end (Ionic/Angular)
Abra um segundo terminal na pasta do repositório do front-end (`Guidaz`):

```bash
# Entre no diretório do front-end
cd Guidaz

# Instale as dependências
npm install

# Inicie o aplicativo no navegador (simulando mobile)
ionic serve
```
Acesse `http://localhost:8100` no seu navegador.

---

## 🔌 Documentação da API & Exemplos para o Postman

Abaixo estão os principais endpoints expostos pelo Back-end para integração com o aplicativo mobile. Você pode importá-los diretamente no **Postman** com a URL base: `http://localhost:8080/api`.

### 👨‍🎓 Alunos (`/api/alunos`)

- **Cadastrar Aluno**
  - **POST**: `http://localhost:8080/api/alunos/cadastrar`
  - *Exemplo de Body (JSON):*
    ```json
    {
      "nome": "João da Silva",
      "email": "joao@email.com",
      "senha": "senha123",
      "telefone": "11988887777"
    }
    ```

- **Listar Todos os Alunos**
  - **GET**: `http://localhost:8080/api/alunos`

- **Atualizar Aluno**
  - **PUT**: `http://localhost:8080/api/alunos/{id}`
  - *Exemplo de URL:* `http://localhost:8080/api/alunos/1`

- **Deletar Aluno**
  - **DELETE**: `http://localhost:8080/api/alunos/{id}`
  - *Exemplo de URL:* `http://localhost:8080/api/alunos/1`

---

### 🚗 Instrutores (`/api/instrutores`)

- **Cadastrar Instrutor**
  - **POST**: `http://localhost:8080/api/instrutores/cadastrar`
  - *Exemplo de Body (JSON):*
    ```json
    {
      "nome": "Carlos Direção",
      "email": "carlos.instrutor@email.com",
      "senha": "senha456",
      "cnh": "12345678900"
    }
    ```

- **Listar Todos os Instrutores**
  - **GET**: `http://localhost:8080/api/instrutores`

- **Atualizar Instrutor**
  - **PUT**: `http://localhost:8080/api/instrutores/{id}`
  - *Exemplo de URL:* `http://localhost:8080/api/instrutores/1`

- **Deletar Instrutor**
  - **DELETE**: `http://localhost:8080/api/instrutores/{id}`
  - *Exemplo de URL:* `http://localhost:8080/api/instrutores/1`

---

## 👥 Equipe e Divisão de Tarefas

Abaixo estão os membros responsáveis pelo desenvolvimento do MVP e suas respectivas áreas de atuação na plataforma:

| Desenvolvedor(a) | Responsabilidade Principal no Código (MVP) |
| :--- | :--- |
| **Stephanie Caroline** | **UX/UI & Autenticação:** Implementação do Design System do Figma, componentização global, rotas iniciais e telas de Login/Cadastro (`pages/auth`). |
| **Ariely Lopes** | **Disponibilidade:** Lógica da tela de `agenda-disponibilidade` do Instrutor, permitindo bloquear horários e gerar os *slots* livres. |
| **Lucas Gabriel** | **Gestão de Alunos:** Estruturação da tela de `gestao-alunos` do Instrutor, incluindo vinculação de pacotes e listagem da base de estudantes. |
| **Anthony Gabriel** | **Agendamento Aluno:** Lógica da tela `agendamento-aulas`, permitindo ao aluno buscar *slots* livres e realizar reservas na agenda do instrutor. |
| **Carlos Gabriel** | **Checkout & Pix:** Lógica das telas `pacotes` e `pagamento-pix`, implementando fluxo de upload/conferência de comprovante Pix. |
| **Gabriel Pacheco** | **Saldo e Histórico:** Lógica das telas `saldo-aulas` e `historico-avaliacoes`, calculando horas contratadas vs. pendentes e sistema de 1 a 5 estrelas. |

---

## 🌿 Como criar e enviar sua própria Branch no GitHub

Se você estiver desenvolvendo uma nova funcionalidade ou correção, é fundamental criar uma branch separada para não alterar o código principal (`main`) diretamente. Siga o passo a passo:

1. **Crie uma nova branch e já mude para ela:**
   ```bash
   git checkout -b nome-da-sua-branch


2. **Adicione os arquivos que você modificou ou criou:**
    ```bash
    git add .

3. **Salve (commit) as suas alterações com uma mensagem descritiva:**
   ```bash
   git commit -m "feat: descrição do que você fez"

4. **Envie a sua nova branch para o GitHub pela primeira vez:**
   ```bash
   git push -u origin nome-da-sua-branch

> (Nota: O parâmetro -u cria a ligação entre a sua máquina e o GitHub. Nas próximas vezes que for enviar atualizações para essa mesma branch, bastará digitar git push).

5. **Volte para a branch principal (main):**
   Quando terminar o trabalho na sua branch ou precisar verificar o código original, você pode mudar de volta para a branch principal usando o comando:
   ```bash
   git checkout main
6. **Atualize a sua branch local (Recomendado):**
   Sempre que voltar para a main, é importante baixar qualquer código novo que possa ter sido aprovado e enviado para o GitHub:
   ```bash
   git pull origin main

## 👥 Colaboradores
Agradecemos às seguintes pessoas que contribuíram para este projeto:

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/anthonygg081" title="Anthony Gabriel">
        <img src="https://github.com/anthonygg081.png" width="100px;" alt="Foto do Anthony"/><br>
        <sub><b>Anthony Gabriel</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/arielyllopes" title="Ariely Lopes">
        <img src="https://github.com/arielyllopes.png" width="100px;" alt="Foto da Ariely"/><br>
        <sub><b>Ariely Lopes</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/carlos-gds" title="Carlos Gabriel">
        <img src="https://github.com/carlos-gds.png" width="100px;" alt="Foto do Carlos"/><br>
        <sub><b>Carlos Gabriel</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/gabrielJv-p" title="Gabriel Pacheco">
        <img src="https://github.com/gabrielJv-p.png" width="100px;" alt="Foto do Gabriel"/><br>
        <sub><b>Gabriel Pacheco</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/lucasand-dev1" title="Lucas Gabriel">
        <img src="https://github.com/lucasand-dev1.png" width="100px;" alt="Foto do Lucas"/><br>
        <sub><b>Lucas Gabriel</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/StephanieCaroll" title="Stephanie Caroline">
        <img src="https://github.com/StephanieCaroll.png" width="100px;" alt="Foto da Stephanie"/><br>
        <sub><b>Stephanie Caroline</b></sub>
      </a>
    </td>
  </tr>
</table>

---

## 📫 Como Contribuir para a Guidaz

Para contribuir com o projeto, siga estas etapas:

1. Faça um fork deste repositório.
2. Crie uma branch para a sua feature:
   ```bash
   git checkout -b minha-feature
   ```
3. Faça suas alterações e confirme-as:
   ```bash
   git commit -m 'feat: nova funcionalidade'
   ```
4. Envie para a branch principal:
   ```bash
   git push origin minha-feature
   ```
5. Abra um Pull Request.

---

## 🤝 Contribuições

Sinta-se à vontade para contribuir com este projeto!

💡 Sugira novas funcionalidades e melhorias.  
🐛 Relate bugs ou problemas encontrados.  
📚 Compartilhe recursos ou ideias para o design e arquitetura.