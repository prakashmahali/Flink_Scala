// Define a case class to represent employee details
case class Employee(empId: Int, name: String, salary: Double)

// Sample list of employee details
val employeeList = List(
  Employee(1, "John", 50000.0),
  Employee(2, "Alice", 60000.0),
  Employee(3, "Bob", 55000.0),
  Employee(4, "Charlie", 70000.0),
  Employee(5, "Emily", 48000.0),
  Employee(6, "David", 75000.0)
)

// Employee IDs to filter
val employeeIdsToFilter = Set(1, 3, 5) // Example: Filter employees with IDs 1, 3, and 5

// Filtered employees
val filteredEmployees = employeeList.filter(emp => employeeIdsToFilter.contains(emp.empId))

// Fetch salaries of filtered employees
val salariesOfFilteredEmployees = filteredEmployees.map(_.salary)

// Print salaries
salariesOfFilteredEmployees.foreach(println)
