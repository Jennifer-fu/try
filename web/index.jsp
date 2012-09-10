
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title></title>
      <script type="text/javascript" src="lib/jquery-1.8.1.js"></script>
      <script type="text/javascript">
          $(document).ready(function(){
              var submitting = false;
              $('#submit').click(function(){
                  if(submitting)return false;
                  submitting = true;
                  $.ajax({
                      url:'upload',
                      type:'post',
                      data:{source:$('#source').val(), destination:$('#destination').val()},
                      success:function(){
                          submitting = false;
                      },
                      error:function(xmlHttpRequest,status,e){
                          $('#error').html(xmlHttpRequest.responseText);
                          submitting = false;

                      }
                  });
                  return false;
              });

              $('#refresh').click(function(){
                  $.ajax( {
                      url:"monitor",
                      type:'get',
                      success:function(data,status,xmlHttpRequest){
                          $('#monitor').html(xmlHttpRequest.responseText);
                      }
                  });
              });
          });
      </script>
  </head>
  <body>
     <form name="uploadForm">
         <input id="source" type="text" size="40">
         <input id="destination" type="text" size="40">
         <input id="submit" type="submit" value="submit">
     </form>
     <a href="#" id="refresh" >Refresh</a>
    <div id="error"></div>
    <div id="monitor"></div>
  </body>
</html>