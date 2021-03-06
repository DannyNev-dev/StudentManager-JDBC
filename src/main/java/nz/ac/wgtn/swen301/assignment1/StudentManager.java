package nz.ac.wgtn.swen301.assignment1;

import nz.ac.wgtn.swen301.studentdb.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A student managers providing basic CRUD operations for instances of Student, and a read operation for instances of Degree.
 * @author Daniel Neville
 */
public class StudentManager {
	static Connection conn;
	static PreparedStatement psReadStudent, psReadDegree, psCreateStudent;
	// DO NOT REMOVE THE FOLLOWING -- THIS WILL ENSURE THAT THE DATABASE IS AVAILABLE
    // AND THE APPLICATION CAN CONNECT TO IT WITH JDBC
    static {
        StudentDB.init();   
        try {
			conn = DriverManager.getConnection("jdbc:derby:memory:studentdb");
			//Initialize prepared statements
			psReadStudent = conn.prepareStatement("SELECT FIRST_NAME,NAME,DEGREE "
					+ "FROM students WHERE ID=?");
			psCreateStudent = conn.prepareStatement("INSERT INTO students "
		    		+ "VALUES (?, ?, ?, ?)");
			psReadDegree = conn.prepareStatement("SELECT NAME FROM degrees WHERE ID=?");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    // DO NOT REMOVE BLOCK ENDS HERE

    // THE FOLLOWING METHODS MUST IMPLEMENTED :
    /**
     * Return a student instance with values from the row with the respective id in the database.
     * If an instance with this id already exists, return the existing instance and do not create a second one.
     * @param id
     * @return
     * @throws NoSuchRecordException if no record with such an id exists in the database
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_readStudent (followed by optional numbers if multiple tests are used)
     */
    public static Student readStudent(String id) throws NoSuchRecordException {   	   	
    	try {
            psReadStudent.setString(1, id);
            ResultSet results = psReadStudent.executeQuery();               
            if (results.next()) {
            	return new Student(id,results.getString(2),results.getString(1),readDegree(results.getString(3)));           
            }
            throw new NoSuchRecordException();
    	}catch(SQLException e) {
    		throw new NoSuchRecordException();
    	}
    }
    /**
     * Return a degree instance with values from the row with the respective id in the database.
     * If an instance with this id already exists, return the existing instance and do not create a second one.
     * @param id
     * @return
     * @throws NoSuchRecordException if no record with such an id exists in the database
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_readDegree (followed by optional numbers if multiple tests are used)
     */
    public static Degree readDegree(String id) throws NoSuchRecordException {
    	String name = "";
    	try {
    		psReadDegree.setString(1, id);
            ResultSet results = psReadDegree.executeQuery();            
            while (results.next()) {                    
                 name = results.getString(1);       
            }
    	}catch(SQLException s) {
    		throw new NoSuchRecordException();
    	}
        return new Degree(id,name);
    }
    /**
     * Delete a student instance from the database.
     * I.e., after this, trying to read a student with this id will result in a NoSuchRecordException.
     * @param student
     * @throws NoSuchRecordException if no record corresponding to this student instance exists in the database
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_delete
     */
    public static void delete(Student student) throws NoSuchRecordException {   	
    	try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM students WHERE ID=\'"+student.getId()+"\'");
    	}catch(SQLException s) {
    		throw new NoSuchRecordException();
    	}
    }
    /**
     * Update (synchronize) a student instance with the database.
     * The id will not be changed, but the values for first names or degree in the database might be changed by this operation.
     * After executing this command, the attribute values of the object and the respective database value are consistent.
     * Note that names and first names can only be max 1o characters long.
     * There is no special handling required to enforce this, just ensure that tests only use values with < 10 characters.
     * @param student
     * @throws NoSuchRecordException if no record corresponding to this student instance exists in the database
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_update (followed by optional numbers if multiple tests are used)
     */
    public static void update(Student student) throws NoSuchRecordException {
    	final String firstName = "UPDATE students SET FIRST_NAME = \'"+student.getFirstName()+"\'  WHERE id = \'"+student.getId()+"\'";
    	final String name = "UPDATE students SET NAME = \'"+student.getName()+"\'  WHERE id = \'"+student.getId()+"\'";
    	final String degree = "UPDATE students SET DEGREE = \'"+student.getDegree().getId()+"\'  WHERE id = \'"+student.getId()+"\'";
    	try {
            Statement s = conn.createStatement();
            s.executeUpdate(firstName);
            s.executeUpdate(name);
            s.executeUpdate(degree);
    	}catch(SQLException s) {
    		throw new NoSuchRecordException();
    	}
    }
    /**
     * Create a new student with the values provided, and save it to the database.
     * The student must have a new id that is not been used by any other Student instance or STUDENTS record (row).
     * Note that names and first names can only be max 1o characters long.
     * There is no special handling required to enforce this, just ensure that tests only use values with < 10 characters.
     * @param name
     * @param firstName
     * @param degree
     * @return a freshly created student instance
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_createStudent (followed by optional numbers if multiple tests are used)
     */
    public static Student createStudent(String name,String firstName,Degree degree) {
    	String id = generateId();       
        try {
			psCreateStudent.setString(1, id);
			psCreateStudent.setString(2, firstName);
			psCreateStudent.setString(3, name);
			psCreateStudent.setString(4, degree.getId());
			psCreateStudent.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return new Student(id,name,firstName,degree);
    }
    /**
     * This method gets all the student IDs and gets the last id in the DB and increments this id
     * to generate the new unique id.
     * @return a new id in the form of a string
     */
    public static String generateId() {
    	ArrayList<String> list = (ArrayList<String>) StudentManager.getAllStudentIds();
    	int id = Integer.parseInt((list.get(list.size()-1)).substring(2)) + 1;
        return Integer.toString(id);	
    }
    /**
     * Get all student ids currently being used in the database.
     * @return
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_getAllStudentIds (followed by optional numbers if multiple tests are used)
     * @throws SQLException 
     */
    public static Collection<String> getAllStudentIds() {    	
    	ArrayList<String> c = new ArrayList<String>(); 
    	try {
	    	Statement stmt = conn.createStatement();
	        ResultSet results = stmt.executeQuery("SELECT id FROM students");           
	        while (results.next()) {               	
	             c.add(results.getString(1));              
	        }	        
    	} catch (SQLException e) {
    			System.out.println("The database is empty or not connected!");
    			e.printStackTrace();
    	}	
        return c;      
    }
    /**
     * Get all degree ids currently being used in the database.
     * @return
     * This functionality is to be tested in test.nz.ac.wgtn.swen301.assignment1.TestStudentManager::test_getAllDegreeIds (followed by optional numbers if multiple tests are used)
     * @throws SQLException 
     */
    public static Iterable<String> getAllDegreeIds() {    
    	ArrayList<String> list = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();     
	        ResultSet results = stmt.executeQuery("SELECT id FROM degrees");           
	        while (results.next()) {               	
	             list.add(results.getString(1));             
	        }    
		} catch (SQLException e) {
			System.out.println("The database is empty or not connected!");
			e.printStackTrace();
		}
        return list;
    }
}
