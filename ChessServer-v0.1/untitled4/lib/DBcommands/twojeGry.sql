SELECT
gamedate,
COALESCE((SELECT name FROM ChessServer.playerdata 
	WHERE (player1id = playerid AND player2id = (SELECT playerid FROM ChessServer.playerdata WHERE name='Kuba')) 
    OR (player1id = (SELECT playerid FROM ChessServer.playerdata WHERE name='Kuba') AND player2id = playerid)), 'X') przeciwnik,
COALESCE(IF((player1id IS NULL AND winner = 1) OR (player2id IS NULL AND winner = 2), 'X', (SELECT name FROM ChessServer.playerdata 
	WHERE (winner=1 AND player1id = playerid) 
	OR (winner=2 AND player2id = playerid))), 'REMIS') zwycięzca
FROM ChessServer.gamedata g
WHERE player1id=(SELECT playerid FROM ChessServer.playerdata WHERE name='Kuba')
OR player2id=(SELECT playerid FROM ChessServer.playerdata WHERE name='Kuba')
ORDER BY gamedate;