 WITH T0 AS (  -- find open project
      SELECT DISTINCT
          SORDERQ.YSOH_PJT_0 AS ProjectNO,
          SORDERQ.YSOQ_PJTORI_0 AS ProjectOriNO,
          SORDERQ.QTY_0 AS SQTY
      FROM EXPLOIT.SORDERQ AS SORDERQ
        WHERE SORDERQ.SALFCY_0 = '#{Site}'
          AND SORDERQ.SOQSTA_0 != 3
      ),
      T1 AS (   -- find open porject
        SELECT
          MFGITM.PJT_0,
          MFGITM.MFGNUM_0,
          DENSE_RANK() OVER (PARTITION BY MFGITM.PJT_0 ORDER BY MFGITM.ROWID) AS SEQ,
          T0.SQTY,
          SUM(MFGITM.EXTQTY_0) OVER (PARTITION BY MFGITM.PJT_0) AS MQTY
        FROM
        T0 INNER JOIN EXPLOIT.MFGITM MFGITM
        ON ( T0.ProjectNO = MFGITM.PJT_0 OR T0.ProjectOriNO = MFGITM.PJT_0 )
        AND MFGITM.MFGFCY_0 = '#{Site}'
        AND MFGITM.ITMSTA_0 !=3                         --- Only open work order
      ),
      T2 AS (
	    SELECT DISTINCT
	    --MFGITM.PJT_0 + ' ' + MFGITM.MFGNUM_0 + ' ' AS Duplicate,
	    MFGITM.PJT_0
	    FROM T1
	    INNER JOIN MFGITM
	      ON MFGITM.PJT_0 = T1.PJT_0
	    WHERE
	      T1.SEQ != 1
	      AND T1.MQTY > T1.SQTY
      )

    SELECT DISTINCT
	    MFGITM.PJT_0 AS ProjectNO,
	    MFGITM.MFGNUM_0 AS WO,
	    MFGITM.CREUSR_0 AS CreateUser,
	    AUTILIS.ADDEML_0 AS CreateUserEmail,
	    MFGITM.CREDAT_0 AS CreateDate
    FROM T2
    INNER JOIN MFGITM
      ON MFGITM.PJT_0 = T2.PJT_0
    LEFT JOIN EXPLOIT.AUTILIS AUTILIS
            ON AUTILIS.USR_0 = MFGITM.CREUSR_0