# HhRuParser
Данный паресер предназначен для считывания информации с hh.ru по заданным параметрам из google.sheets. 
Всю работу парсера можно разделить на три этапа: считыание входных параметров, парсинг данных по входным пармараметрам(методы:buildUrl, fetchVacancyData), запись рассчитанных значений в sheets(методы:assemblyRange,updateValueResponse,writeValueResponse).