select distinct showner.user_code '爱豆编号',
        bbank.card_holder_name as '持卡人姓名',
        bbank.bank_name as '开户行名称',
        bbank.bank_card_num as '开户行银行卡号',
        bbank.card_holder_idnum as '持卡人身份证号',
        bbank.card_holder_phone as '持卡人电话号码',
from  idunivers_shop_owners showner
left join  idunivers_students_bbank bbank on(bbank.user_id = showner.user_id)
where  bbank.status =0 or bbank.status is null




    input { file { path => "/home/elk/elk/teaching/elasticsearch-7.7.0/logs/my-elk.log" start_position => "beginning" codec => multiline { pattern => "^\[" negate => true
what => "previous" } } }output { elasticsearch { hosts => ["http://172.18.194.140:9200"] index => "es-log-%{+YYYY.MM.dd}" }stdout{} }
