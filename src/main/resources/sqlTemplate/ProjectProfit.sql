  WITH  
  T01 AS (
    SELECT DISTINCT
      SORDERQ.YSOQ_PJTORI_0 AS PJT_0
    FROM
      EXPLOIT.SORDERQ SORDERQ
    WHERE
      SORDERQ.SALFCY_0 = '#{Site}'
      AND SORDERQ.YSOQ_PJTORI_0 != ''
      AND SORDERQ.SOQSTA_0 != 3  --- only open sales order
  ),  
  T02 AS (    
    SELECT DISTINCT
      SORDERQ.YSOH_PJT_0  AS PJT_0
    FROM
      EXPLOIT.SORDERQ SORDERQ
    WHERE
      SORDERQ.SALFCY_0 = '#{Site}'
      AND SORDERQ.SOQSTA_0 != 3  --- only open sales order
  ),
  T03 AS (
   SELECT 
     T01.PJT_0
   FROM T01    
   UNION    
   SELECT 
    T02.PJT_0
   FROM T02
  ),
  T00 AS (
    SELECT DISTINCT
      T03.PJT_0
    FROM
      T03
  ),
  T0 AS ( ---- Project sales line price include tax
    SELECT DISTINCT
      IIF(SORDERQ.YSOQ_PJTORI_0 = '', SORDERQ.YSOH_PJT_0, SORDERQ.YSOQ_PJTORI_0) AS ProjectNO,
      SORDERQ.SOHNUM_0,
      SORDERP.ITMREF_0,
      SORDERP.ITMDES_0,
      SORDERP.TSICOD_1,
      SORDERQ.QTY_0,
      SORDERP.NETPRIATI_0,
      SORDERP.NETPRIATI_0 * SORDERQ.QTY_0 AS ProjectSalesPrice,
      SORDERP.CREDAT_0
    FROM T00
      INNER JOIN EXPLOIT.SORDERQ SORDERQ
        ON (T00.PJT_0 = SORDERQ.YSOH_PJT_0 OR T00.PJT_0 = SORDERQ.YSOQ_PJTORI_0)
    INNER JOIN EXPLOIT.SORDERP SORDERP
        ON SORDERP.SOHNUM_0 = SORDERQ.SOHNUM_0
      AND SORDERP.SOPLIN_0 = SORDERQ.SOPLIN_0
      AND SORDERQ.SALFCY_0 = '#{Site}'
      AND SORDERP.SALFCY_0 = '#{Site}'
      AND SORDERP.NETPRIATI_0 > 0
  ),
  T1 AS (
    SELECT
      T0.ProjectNO,
      T0.SOHNUM_0 AS OrderNO,
      T0.TSICOD_1 AS ProductFamily,
      T0.ITMREF_0 AS PN,
      T0.ITMDES_0 AS Description,
      T0.QTY_0 AS QTY,
      T0.CREDAT_0 AS OrderDate,
      SORDER.CUR_0 AS SalesCurrency,
      SORDER.CHGRAT_0 AS Rate,
      T0.ProjectSalesPrice,
      T0.ProjectSalesPrice * ( SORDER.ORDINVATIL_0 / SORDER.ORDINVATI_0) AS ProjectSalesLocalPrice
    FROM
      T0
    INNER JOIN EXPLOIT.SORDER SORDER
        ON T0.SOHNUM_0 = SORDER.SOHNUM_0
  ),
  T2 AS (
    SELECT DISTINCT
          PORDERP.PJT_0,
          PORDERQ.CPRCUR_0 AS LocalCurrency,
          Sum(PORDERQ.LINAMTCPR_0 + PORDERQ.AMTTAXLIN1_0 + PORDERQ.AMTTAXLIN2_0 + PORDERQ.AMTTAXLIN3_0) OVER (PARTITION BY PORDERP.PJT_0) AS ProjectLocalCost
    FROM T00
      INNER JOIN EXPLOIT.PORDERP AS PORDERP
          ON T00.PJT_0 = PORDERP.PJT_0
    INNER JOIN EXPLOIT.PORDERQ AS PORDERQ
          ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
        AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
    WHERE
      PORDERQ.PRHFCY_0 = '#{Site}'
      AND PORDERP.PRHFCY_0 = '#{Site}'
  )

  SELECT
    T1.*,
    T2.LocalCurrency,
    T2.ProjectLocalCost,
    T1.ProjectSalesLocalPrice - T2.ProjectLocalCost AS Profit,
    T1.ProjectSalesLocalPrice / T2.ProjectLocalCost AS ProfitRate
  FROM
    T1
  LEFT JOIN T2 ON T1.ProjectNO = T2.PJT_0
  WHERE
    T1.ProjectSalesLocalPrice / T2.ProjectLocalCost < #{ProfitRate}
  ORDER BY
    ProfitRate ASC,
    Profit ASC,
    T1.OrderDate DESC