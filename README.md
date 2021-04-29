## blog posting
https://jinyoungchoi95.tistory.com/33

## Develop Environment

- IntelliJ
- Java 11
- Gradle - 6.8.3
- MySQL
- thymeleaf
- Spring boot - 2.4.5



## SQL Injection?

 SQL을 사용하다보면 SQL Injection에 주의해서 Parameter Binding을 해라 라는 말이 자주 보인다. 과연 SQL Injection이 어떤 공격이며 Parameter Binding을 어떻게 해서 그 공격을 어떻게 막는 것일까?

 SQL Injection, 직역하면 SQL 삽입이다. 클라이언트에서 입력값을 조작함으로써 서버의 데이터베이스를 공격하는 방법으로 사용자가 입력한 데이터를 필터링없이 그대로 받아들였을 때 발생한는 공격이다. 최근에는 너무 잘 알려진 취약점 공격이고 서버에서도 SQL Injection에 대해서 방어가 되어있다. 그럼 이 공격이 어떻게 들어오는지 먼저 알아보도록 하자



## SQL Injection 공격

 다음과 같은 로그인 폼이 있다고 하자.
<p align="center">
<img width="612" alt="1" src="https://user-images.githubusercontent.com/69106910/116495353-b1d99e80-a8dd-11eb-8de9-3b787c6d4cf9.png">
</p>

 단순히 아이디와 비밀번호를 입력받으면 yes 페이지를, 아니면 no 페이지를 주는 웹 어플리케이션이다. 서버단에서는 SQL에서 id와 password가 일치하는 레코드가 있다면 yes 페이지를, 아니면 no를 준다.

<p align="center">
<img width="289" alt="2" src="https://user-images.githubusercontent.com/69106910/116495364-b7cf7f80-a8dd-11eb-8ef5-3ebc1a0e8068.png">
</p>

 임의로 user 테이블에는 id가 `user1`, password가 `pass1`인 유저 단 한명만 있는 경우로 만들었다. 그럼 이 웹 서비스는 user1이 pass1으로 로그인하지 않는 이상 로그인 페이지  가 나오지 않을 것이라는 것이 일반적인 생각이다. 근데 여기에는 심각한 취약점이 있어서 다음 방법으로 로그인이 가능하다.

<p align="center">
  <img src="https://user-images.githubusercontent.com/69106910/116495369-b9994300-a8dd-11eb-864c-5f7664fe8944.png">
</p>


<p align="center">
  <img src="https://user-images.githubusercontent.com/69106910/116495373-bbfb9d00-a8dd-11eb-9daf-fe62e773719e.png">
</p>


 ??? `user1 / pass1`만 로그인 가능한 이 서비스에서 이상한 문자를 넣은 상태에서 로그인이 가능하다. 이게 왜 이럴까?

 지금부터 확인할 로그인을 확인하는 쿼리에서 지금 넣은 쿼리파라미터를 넣어보면 뭔가 이상한 점을 느낄 수 있다.



```mysql
select * from user where id='" + id + "' and pw='" + pw + "'"
```

 기본적인 로그인 확인을 하기 위한 입력 폼이다. 입력한 id와 pw가 같으면 해당 유저가 있으므로 검색이 된다.

```mysql
select * from user where id='user1' and pw='pass1';
```

 `user1 / pass1`으로 user 테이블 내에서 유저가 있음을 확인하였기 때문에 이 구문은 정상적으로 로그인을 위한 쿼리가 되겠다.



 근데 지금 공격에 이용한 쿼리파라미터를 사용해서 쿼리를 완성하면 어떻게 될까?

```mysql
select * from user where id='i_attack_you' and pw='zz' or '1'='1';

#해당 구문과 같음.
select * from user;
```

 이전에는 id와 pw만 `and`를 통해서 둘다 일치했을 경우에만 쿼리가 돌아갔지만 뒤에 `or '1'='1'`가 추가되어버렸다. `'1'='1'`은 알다시피 항상 참이고 `or`가 붙었기 때문에 `id='i_attack_you' and pw='zz'`가 참이든 거짓이든 간에 이 쿼리는 where 조건절이 항상 참인 쿼리가 되었다.

 따라서 쿼리를 db서버로 보냈을 때 항상 레코드를 주며 로그인이 되었던 것이다.



 이런 논리적인 오류를 사용하는 SQL Injection도 있고 주석, MySQL로 따지면 `#`과 `--`같은 주석으로 SQL Injection을 거는 경우도 있다. 다음과 같이 보내면 어떻게 될까? 

<p align="center">
  <img src="https://user-images.githubusercontent.com/69106910/116495375-bdc56080-a8dd-11eb-944e-81897b7c8a0d.png">
</p>


```mysql
select * from user where id='user1';drop table user#' and pw='sdf'
```

 id와 pw 공간 내부에 각각 파라메터로 들어온 값을 넣으면 위와 같은 sql 구문이 생성된다. 생각보다 복잡해 보이지만 #을 통해서 뒤에 구문이 사라졌으므로 단 두개의 쿼리가 남는다.

- `select * from user where id = 'user1';`
- `drop table user`

 사실 mysql jdbc에서는 멀티쿼리를 지원하지 않기 때문에 해당 구문은 에러가 나지만 injection 예시를 들기 위해서 허용해둔 상태이다.

 그럼 이 쿼리가 동작한다면? user 테이블이 드랍된다...

<p align="center">
<img width="130" alt="6" src="https://user-images.githubusercontent.com/69106910/116495385-c027ba80-a8dd-11eb-80ab-a049496ef097.png">
</p>


 지금처럼 논리적 오류, 주석을 사용한 SQL injection 이외에도 사실 다양한 SQL Injection이 존재하기는 한다. union 명령어를 이용한 SQL Injection, Boolean을 이용한 SQL Injection... 너무나도 많다.

 이런 SQL Injection은 결국 서버단에서 SQL을 생성하고 파라메터를 받아들일때 방어를 해야한다. 그럼 어떻게 방어를 해야하는지 알아보자.





## SQL Injection 방어

 사실 너무나 간단하다. 입력값에 대해서 예외처리를 통해서 검증을 해주면 된다. 이때 사용하는 것이 앞서 말한 파라메터 바인딩이다. 기존의 Injection을 허용했던 코드를 보자.

```java
//SQL Injection query
@Transactional
public boolean findUserByIdAndPwError(String id, String pw) throws Exception {
    Class.forName(Driver);
    ResultSet rs = null;
    Statement stmt = null;
    String sql = "select * from user where id='" + id + "' and pw='" + pw + "'";
    log.info(sql);

    try {
        Connection con = DriverManager.getConnection(Url, user, password);
        stmt = con.createStatement();

        rs = stmt.executeQuery(sql);
        if(rs.next()) return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
```

<p align="center">
<img width="1443" alt="7" src="https://user-images.githubusercontent.com/69106910/116495391-c1f17e00-a8dd-11eb-89b9-da59c39201b6.png">
</p>

 사실 들어온 sql구문에 대해서 검증이나 예외처리를 하는 구문조차 없다. 따라서 이러한 코드는 Injection이 들어오는 것이다.

 그럼 Injection을 파라메터 바인딩한 코드는 어떻게 생겼을까?

```java
//defence SQL Injection by parameter binding
@Transactional
public boolean findUserByIdAndPw(String id, String pw) throws Exception {
    Class.forName(Driver);
    ResultSet rs = null;
    PreparedStatement pstmt = null;
    String sql = "select * from user where id=? and pw=?";
    log.info(sql);

    try {
        Connection con = DriverManager.getConnection(Url, user, password);
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, pw);
        String sqlChanged = pstmt.toString();
        log.info(sqlChanged);
        rs = pstmt.executeQuery();
        if(rs.next()) return true;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
```

<p align="center">
<img width="1558" alt="8" src="https://user-images.githubusercontent.com/69106910/116495395-c3bb4180-a8dd-11eb-91ef-d70bf15a16a0.png">
</p>

 PreparedStatement 인터페이스를 사용하여 구현함으로써 `setString()`메소드를 사용하여 데이터를 집어 넣는다. 똑같은 파라메터를 넣고 우리가 생각하기에 똑같은 쿼리가 들어갔는데 결과가 다르다.

 or 연산자의 논리 오류를 이용해서 로그인 시도도 되지않았고, table이 drop되지도 않았다. 서버로 보낸 쿼리의 결과를 보면 그 이유를 알 수 있다.



```mysql
select * from user where id='user1' and pw='zz'' or ''1''=''1'
```

```mysql
select * from user where id='user1''; drop table user#' and pw='zz'' or ''1''=''1'
```

 `'`가 하나 더 붙어버리면서 or연산자나 # 주석처리나 모두 방어해버린다. 결국 PreparedStatement의 `setString()`을 통해서 검증을 함으로써 파라메터 바인딩이 된 것이다.

 `setString()`의 내용만 살짝 맛만 보면

<p align="center">
<img width="637" alt="9" src="https://user-images.githubusercontent.com/69106910/116495399-c5850500-a8dd-11eb-94c5-5544e25777ab.png">
</p>

 5번째 케이스를 봐보자. or 논리오류나 drop에서 우리가 sql문을 사용할 때 `'`를 사용했지만 이미 `setString()`에서는 이걸 알고 알아서 검증처리를 통해 공격대로 안넘어오게 방어한 것을 알 수 있다.

 자주 사용하는 JPA 역시 무의식적으로 `save()`와 같은 메소드를 사용하지만 JPA에서도 SQL Injection에 대해서 파라메터 바인딩을 통해 막아준다. 단, 마찬가지로 쿼리를 사용할 경우 위와 같은 파라메터 바인딩을 통한 예외처리는 필수적으로 행해주어야 할 것이다.



 물론 문법마다 다양한 차이점이 있다. MySQL, Oracle, MSSQL ... 각각의 데이터베이스들이 각각의 문법이 다 다르기 때문에 파라메터 바인딩을 함에 있어서도 차이점은 물론 있을 것이다.

 문제는 문법마다 차이가 있기 때문에 상대쪽에서 내가 사용하는 DB가 무엇인지 알면 그 문법대로 공격이 들어올 수 있다는 점이다. 따라서 페이지 에러메시지를 클라이언트로 전송할 때 DB 정보를 숨기고 보내는 것이 매우 매우 중요하다는 것을 생각해낼 수 있다.

