/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "username VARCHAR(20), " +
              "password VARCHAR(20), " +
              "email VARCHAR(30))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS project(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "name VARCHAR(20) NOT NULL)");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_project (" +
              "id INT NOT NULL PRIMARY KEY, " +
              "user_id INT NOT NULL, " +
              "project_id INT NOT NULL, " +
              "role ROLE, " +
              "CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id), " +
              "CONSTRAINT fk_project FOREIGN KEY(project_id) REFERENCES project(id))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS list(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "project_id INT NOT NULL, " +
              "name VARCHAR(20) NOT NULL, " +
              "CONSTRAINT fk_project FOREIGN KEY(project_id) REFERENCES project(id))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS task(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "list_id INT NOT NULL, " +
              "name VARCHAR(20) NOT NULL, " +
              "date_to_start DATE NOT NULL, " +
              "date_to_finish DATE NOT NULL, " +
              "priority INT NOT NULL, " +
              "assigned_user_id INT, " +
              "creator_id INT, " +
              "description VARCHAR(10000), " +
              "time_estimation INTERVAL, " +
              "CONSTRAINT fk_list FOREIGN KEY(list_id) REFERENCES list(id), " +
              "CONSTRAINT fk_assigned_user_id FOREIGN KEY(assigned_user_id) REFERENCES users(id), " +
              "CONSTRAINT fk_creator_id FOREIGN KEY(assigned_user_id) REFERENCES users(id))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS message(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "user_id INT NOT NULL, " +
              "task_id INT NOT NULL, " +
              "text VARCHAR(1000) NOT NULL, " +
              "time TIME NOT NULL, " +
              "CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id), " +
              "CONSTRAINT fk_task FOREIGN KEY(task_id) REFERENCES task(id))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS action(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "user_id INT NOT NULL, " +
              "task_id INT NOT NULL, " +
              "list_id INT NOT NULL, " +
              "CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id), " +
              "CONSTRAINT fk_list FOREIGN KEY(list_id) REFERENCES list(id), " +
              "CONSTRAINT fk_task FOREIGN KEY(task_id) REFERENCES task(id))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tag(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "name VARCHAR(40))"
              );
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tag_task(" +
              "id INT NOT NULL PRIMARY KEY, " +
              "tag_id INT NOT NULL, " +
              "task_id INT NOT NULL, " +
              "CONSTRAINT fk_tag FOREIGN KEY(tag_id) REFERENCES tag(id), " +
              "CONSTRAINT fk_task FOREIGN KEY(task_id) REFERENCES task(id))"
      );
      
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
