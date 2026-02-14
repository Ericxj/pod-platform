import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="123456", # Assuming default password or from config
        database="pod_system"
    )
    cursor = conn.cursor(dictionary=True)
    
    user_id = 1
    
    # 1. Check User
    cursor.execute("SELECT * FROM iam_user WHERE id = %s", (user_id,))
    print(f"User: {cursor.fetchall()}")
    
    # 2. Check User Role
    cursor.execute("SELECT * FROM iam_user_role WHERE user_id = %s", (user_id,))
    roles = cursor.fetchall()
    print(f"User Roles: {roles}")
    
    role_ids = [r['role_id'] for r in roles]
    print(f"Role IDs: {role_ids}")

    # 3. Check Data Scope
    query = """
        SELECT ds.* FROM iam_data_scope ds 
        WHERE ds.scope_type = 'FACTORY' 
        AND ds.deleted = 0 
        AND (
          (ds.subject_type = 'USER' AND ds.subject_id = %s) 
          OR 
          (ds.subject_type = 'ROLE' AND ds.subject_id IN (SELECT role_id FROM iam_user_role WHERE user_id = %s AND deleted = 0))
        )
    """
    cursor.execute(query, (user_id, user_id))
    scopes = cursor.fetchall()
    print(f"Scopes: {scopes}")

    conn.close()
except Exception as e:
    print(f"Error: {e}")
