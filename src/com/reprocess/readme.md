#数据处理记录
##8月2日
+ 数据噪音问题
> （1）在将安居客、房天下和我爱我家的数据导入mongodb的时候，发现unitprice的值
   还是存在很多不对的地方，有的是因为各个属性值的错位造成的，有的是本身就无数值只
>  有单位。

##8月3日
+ 数据查询问题
> (1)按月份调用mongodb中的数据的时候，发现数据只调用了一部分，或者根本不满足调用条件，
  调试之后才发现是月份的输入问题。
  当月份为两位数时：
  condition.put("month",i);应该改成condition.put("month","0"+i); 
>  这个问题让我考虑到很有可能里面的月份数据也不规则，有的是month=7，有些则是month=07，
>  考虑到以后数据库查询的方便，还是将数据全部变成不带0的比较好，统一时间描述标准。 
>  也可以进行两次查询，第一次查询condition.put("month",i)，第二次再查询condition.put("month","0"+i)
>  这样就不会落掉数据啦。数据计算存入本地后，再作数据对比与合并处理  
> (2)还有在算unitprice的时候，需要验证除数是否为0，当除数为0时，会抛出异常
  