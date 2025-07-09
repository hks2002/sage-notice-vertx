   WITH
    T0 AS (
        SELECT DISTINCT
            PORDERP.POHNUM_0,
            PORDERP.POPLIN_0,
            PORDERP.PJT_0,
            PORDERP.ITMREF_0,
            RTRIM(PORDERP.ITMDES1_0 +' '+ PORDERP.ITMDES2_0 +' '+ PORDERP.ITMDES3_0) AS Description,
            PORDERQ.QTYSTU_0,
            PORDERQ.LINATIAMT_0,
            PORDERQ.NETCUR_0,
            PORDERP.CREDAT_0,
            PORDERP.CREUSR_0,
            AUTILIS.ADDEML_0
        FROM
            EXPLOIT.PORDERP AS PORDERP
        INNER JOIN EXPLOIT.PORDERQ AS PORDERQ 
            ON PORDERP.POHNUM_0 = PORDERQ.POHNUM_0
               AND PORDERP.POPLIN_0 = PORDERQ.POPLIN_0
        LEFT JOIN EXPLOIT.AUTILIS AUTILIS
            ON AUTILIS.USR_0 = PORDERP.CREUSR_0
        WHERE
            PORDERP.PRHFCY_0 = '#{Site}'
            AND PORDERQ.LINCLEFLG_0 = 1 --- line open flag, exclude manual closed po line
            AND PORDERP.NETPRI_0 > 0 --- exclude manually mark 0, it's an error line
            AND SUBSTRING(PORDERP.POHNUM_0, 2, 2) != 'CT' --- exclude delivery
            ),
    T1 AS (
        SELECT DISTINCT
            PRECEIPTD.POHNUM_0,
            PRECEIPTD.POPLIN_0
        FROM
            EXPLOIT.PRECEIPTD AS PRECEIPTD
        WHERE
            PRECEIPTD.PRHFCY_0 = '#{Site}'
    ),
    T2 AS (
        SELECT
            T0.POHNUM_0,
            T0.POPLIN_0,
            T0.PJT_0,
            T0.ITMREF_0,
            T0.Description,
            T0.QTYSTU_0,
            T0.LINATIAMT_0,
            T0.NETCUR_0,
            T0.CREDAT_0,
            T0.CREUSR_0,
            T0.ADDEML_0
        FROM
            T0
        LEFT JOIN T1 ON T1.POHNUM_0 = T0.POHNUM_0
            AND T1.POPLIN_0 = T0.POPLIN_0
        WHERE
            T1.POHNUM_0 IS NULL
            AND T1.POPLIN_0 IS NULL
    ),
    T3 AS (
        SELECT DISTINCT
            IIF(SORDERQ.YSOQ_PJTORI_0 = '', SORDERQ.YSOH_PJT_0, SORDERQ.YSOQ_PJTORI_0) AS ProjectNO,
            SORDERQ.SOHNUM_0 AS OrderNO,
            SORDERQ.ITMREF_0 AS PN,
            SORDERQ.QTY_0 AS Qty,
            SORDER.ORDDAT_0 AS OrderDate
        FROM
            EXPLOIT.SORDERQ AS SORDERQ
        INNER JOIN EXPLOIT.SORDER SORDER 
          ON SORDERQ.SOHNUM_0 = SORDER.SOHNUM_0
            AND SORDER.SALFCY_0 = '#{Site}'
            AND SORDERQ.SALFCY_0 = '#{Site}'
            AND SORDERQ.SOQSTA_0 = 3
    )

    SELECT DISTINCT
        T2.POHNUM_0 AS PurchaseNO,
        T2.POPLIN_0 AS PurchaseLine,
				T2.ITMREF_0 AS PurchasePN,
        T2.Description,
        T2.QTYSTU_0 AS PurchaseQty,
        T2.LINATIAMT_0 AS PurchaseAmount,
        T2.NETCUR_0 AS PurchaseCurrency,
        T2.ADDEML_0 AS PurchaserEmail,
        T2.CREDAT_0 AS PurchaseDate,
        T2.CREUSR_0 AS Purchaser,
		    T3.ProjectNO,
        T3.OrderNO,
        T3.PN AS SalesPN,
        T3.Qty AS SalesQty,
        T3.OrderDate
    FROM
        T2
    INNER JOIN T3 ON T2.PJT_0 = T3.ProjectNO
    ORDER BY
        T3.OrderDate ASC