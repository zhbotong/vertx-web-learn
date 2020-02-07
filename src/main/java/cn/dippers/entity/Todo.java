package cn.dippers.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.function.Function;

public class Todo {
 private Long id;

  public String content; //任务

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime time; //时间
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;//创建时间

  public static Function<Row,Todo> mapper = row -> {
    Todo todo = new Todo();
    todo.setId(row.getLong("id"));
    todo.setContent(row.getString("content"));
    todo.setCreateTime(row.getLocalDateTime("createTime"));
    todo.setTime(row.getLocalDateTime("time"));
    return todo;
  };

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }
}
