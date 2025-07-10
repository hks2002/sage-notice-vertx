   WITH
    T0 AS (      -- find project with seq
        SELECT DISTINCT
          PORDERP.PJT_0,
          PORDERP.ITMREF_0,
          DENSE_RANK() OVER (PARTITION BY PORDERP.PJT_0, PORDERP.ITMREF_0 ORDER BY PORDERP.ROWID) AS SEQ
        FROM
          EXPLOIT.PORDERP AS PORDERP
        INNER JOIN EXPLOIT.PORDERQ PORDERQ
            ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
            AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
        LEFT JOIN EXPLOIT.TEXCLOB TEXCLOB_PO_LIN
            ON PORDERQ.LINTEX_0 = TEXCLOB_PO_LIN.CODE_0
        LEFT JOIN EXPLOIT.PRECEIPTD AS PRECEIPTD
            ON PRECEIPTD.POHNUM_0 = PORDERP.POHNUM_0
            AND PRECEIPTD.POPLIN_0 = PORDERP.POPLIN_0
        LEFT JOIN EXPLOIT.TEXCLOB TEXCLOB_PT_LIN
            ON PRECEIPTD.LINTEX_0 = TEXCLOB_PT_LIN.CODE_0
        WHERE
          PORDERP.PRHFCY_0 = '#{Site}'
        AND RIGHT(PORDERP.PJT_0, 4) != '0001'
        AND RIGHT(PORDERP.PJT_0, 8) != 'ZLOGISTIC'
        AND PORDERP.PJT_0 != 'TQUALITE'
        ---FACHDIV0001:   PURCHASE VARIOUS
        ---FACHTRADRTD:   INBOUND CUSTOM FEES
        ---FVENTRAAUT:    OUTBOUND SHIPMENT FEES
        ---FACHTRAAUT:    INBOUND SHIPMENT FEES
        AND PORDERP.ITMREF_0 NOT IN ('FACHDIV0001', 'FACHTRADRTD', 'FVENTRAAUT','FACHTRAAUT')
        ---WORD 'AGAIN'marking ignored
        AND TEXCLOB_PO_LIN.TEXTE_0 NOT LIKE '%AGAIN%'
        AND TEXCLOB_PT_LIN.TEXTE_0 NOT LIKE '%AGAIN%'
    ),
    T1 AS (    -- find project which has more than one seq
        SELECT DISTINCT
          T0.PJT_0,
          T0.ITMREF_0
        FROM
          T0
        WHERE
          T0.SEQ > 1
    ),
    T2 AS (   -- find purchase line for sales order line
        SELECT DISTINCT
            PORDERP.PJT_0,
            PORDERP.ITMREF_0,
            SUM(PORDERQ.QTYSTU_0) OVER (PARTITION BY PORDERP.ITMREF_0, PORDERP.PJT_0 ) AS PQTY,
            SORDERQ.QTYSTU_0 AS SQTY
        FROM  T1
        INNER JOIN EXPLOIT.PORDERP AS PORDERP
            ON T1.PJT_0 = PORDERP.PJT_0
            AND T1.ITMREF_0 = PORDERP.ITMREF_0
        INNER JOIN EXPLOIT.PORDERQ AS PORDERQ
            ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
            AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
        INNER JOIN EXPLOIT.SORDERQ SORDERQ
            ON (SORDERQ.YSOH_PJT_0 = T1.PJT_0 OR SORDERQ.YSOQ_PJTORI_0 = T1.PJT_0)
        WHERE
            PORDERP.PRHFCY_0 = '#{Site}'
        AND PORDERQ.PRHFCY_0 = '#{Site}'
        AND SORDERQ.SALFCY_0 = '#{Site}'
        AND PORDERQ.LINCLEFLG_0 != 2            -- only open purchase order line  1: open, 2: closed
    ),
    T3 AS (    -- find purchase line of Work order for stock project
        SELECT DISTINCT
            PORDERP.PJT_0,
            PORDERP.ITMREF_0,            
            SUM(PORDERQ.QTYSTU_0) OVER (PARTITION BY PORDERP.ITMREF_0, PORDERP.PJT_0 ) AS PQTY,
            SUM(MFGITM.EXTQTY_0) OVER (PARTITION BY MFGITM.PJT_0 ) AS SQTY
        FROM  T1
        INNER JOIN EXPLOIT.PORDERP AS PORDERP
            ON T1.PJT_0 = PORDERP.PJT_0
            AND T1.ITMREF_0 = PORDERP.ITMREF_0
        INNER JOIN EXPLOIT.PORDERQ AS PORDERQ
            ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
            AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
        INNER JOIN EXPLOIT.MFGITM MFGITM
            ON MFGITM.PJT_0 = PORDERP.PJT_0        
        INNER JOIN EXPLOIT.OPPOR OPPOR
            ON MFGITM.PJT_0 = OPPOR.OPPNUM_0
        LEFT JOIN EXPLOIT.AUTILIS AUTILIS
            ON AUTILIS.USR_0 = PORDERP.CREUSR_0
        WHERE
            PORDERP.PRHFCY_0 = '#{Site}'
        AND PORDERQ.PRHFCY_0 = '#{Site}'
        AND MFGITM.MFGFCY_0 = '#{Site}'
        AND MFGITM.ITMSTA_0 !=3                         --- Only open work order
        AND OPPOR.OPPTYP_0 = 'ART'                      --- ProjectNO is Stock Project
        AND PORDERQ.LINCLEFLG_0 != 2                    -- only open purchase order line  1: open, 2: closed
    ),
   T4 AS (
    SELECT
        T2.PJT_0 AS ProjectNO,
        T2.ITMREF_0 AS PN,        
        RTRIM(PORDERP.ITMDES1_0 +' '+ PORDERP.ITMDES2_0 +' '+ PORDERP.ITMDES3_0) AS Description,
        PORDERP.POHNUM_0 AS PurchaseNO,
        PORDERP.POPLIN_0 AS PurchaseLine,
        PORDERQ.QTYSTU_0 AS PurchaseQty,
        PORDERQ.LINATIAMT_0 AS Cost,
        PORDERQ.NETCUR_0 AS Currency,
        PORDERP.CREDAT_0 AS PurchaseDate,
        T2.PQTY AS TotalPurchaseQty,
        T2.SQTY  AS TotalSalesQty,
        PORDERP.CREUSR_0 AS Purchaser,
        AUTILIS.ADDEML_0 AS PurchaserEmail,
        DENSE_RANK() OVER (PARTITION BY PORDERP.PJT_0, PORDERP.ITMREF_0 ORDER BY PORDERP.ROWID ) AS Seq
    FROM
        T2
    INNER JOIN T2 AS T4
    ON T2.PJT_0 = T4.PJT_0
        AND T2.ITMREF_0 = T4.ITMREF_0           
    INNER JOIN EXPLOIT.PORDERP AS PORDERP
        ON T2.PJT_0 = PORDERP.PJT_0
        AND T2.ITMREF_0 = PORDERP.ITMREF_0
    INNER JOIN EXPLOIT.PORDERQ AS PORDERQ
        ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
        AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
    LEFT JOIN EXPLOIT.AUTILIS AUTILIS
        ON AUTILIS.USR_0 = PORDERP.CREUSR_0
    WHERE
       T2.PQTY > T2.SQTY      -- if total purchase Qty is less sales Qty, batch purchase, ignore it

    UNION

    SELECT
        T3.PJT_0 AS ProjectNO,
        T3.ITMREF_0 AS PN,        
        RTRIM(PORDERP.ITMDES1_0 +' '+ PORDERP.ITMDES2_0 +' '+ PORDERP.ITMDES3_0) AS Description,
        PORDERP.POHNUM_0 AS PurchaseNO,
        PORDERP.POPLIN_0 AS PurchaseLine,
        PORDERQ.QTYSTU_0 AS PurchaseQty,
        PORDERQ.LINATIAMT_0 AS Cost,
        PORDERQ.NETCUR_0 AS Currency,
        PORDERP.CREDAT_0 AS PurchaseDate,
        T3.PQTY AS TotalPurchaseQty,
        T3.SQTY  AS TotalSalesQty,
        PORDERP.CREUSR_0 AS Purchaser,
        AUTILIS.ADDEML_0 AS PurchaserEmail,
        DENSE_RANK() OVER (PARTITION BY PORDERP.PJT_0, PORDERP.ITMREF_0 ORDER BY PORDERP.ROWID ) AS Seq
    FROM
        T3
        INNER JOIN T3 AS T5
        ON T3.PJT_0 = T5.PJT_0
           AND T3.ITMREF_0 = T5.ITMREF_0          
        INNER JOIN EXPLOIT.PORDERP AS PORDERP
            ON T3.PJT_0 = PORDERP.PJT_0
            AND T3.ITMREF_0 = PORDERP.ITMREF_0
        INNER JOIN EXPLOIT.PORDERQ AS PORDERQ
            ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
            AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
        LEFT JOIN EXPLOIT.AUTILIS AUTILIS
            ON AUTILIS.USR_0 = PORDERP.CREUSR_0
    WHERE
       T3.PQTY > T3.SQTY       -- if total purchase Qty is less sales Qty, batch purchase, ignore it
 )
 SELECT * FROM T4 
 ORDER BY Seq ASC