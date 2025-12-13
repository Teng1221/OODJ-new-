# oodj


# 📘 Course Recovery System (CRS)

This project is for our **OODJ Assignment**.  
It is built in **Java** using **Maven** for dependency management.  

---

## 1️⃣ Install Java
1. Download **Java JDK 17 (or above)**:  
   [https://adoptium.net/](https://adoptium.net/)  

2. Install and make sure Java works:  
   ```bash
   java -version
   ```

## 2️⃣ Install Maven

1. Download Maven: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)

2. Extract it into:

   ```bash
   C:\Program Files\Apache\Maven\
   ```

3. Add the bin folder to PATH in Environment Variables.

4. Check it works:
   ```bash
   mvn -v
   ```
## 3️⃣ Install Cursor (IDE)

1. Download Cursor: [https://cursor.sh/](https://cursor.sh/)

2. Open this repo in Cursor:
   ```bash 
   cursor .
   ```
## 4️⃣ Running the Project
Install dependencies and build
   ```bash
   mvn clean install
   ```
Run the project
```bash
mvn exec:java -Dexec.mainClass="edu.apu.crs.App"
```

This will compile and run the main application.

## 5️⃣ Git Workflow (VERY IMPORTANT 🚨)

Think of Git like writing a class book:

- `main` = the final book everyone reads.

- Your branch = your draft pages.

- We copy from drafts into the final book, then bring the updated book back to drafts so nothing is lost.

## ⭐ Rules for Team Work

1. Pull before you start writing (get the latest book):

   ```bash 
   git pull origin main
   ```
2. Make your own draft (branch):
   ```bash
   git checkout -b feature-usermanagement
   ```

3. Save your work:
   ```bash
   git add .
   git commit -m "Added Login Page GUI"
   git push origin feature-usermanagement
   ```

4. Wait for review before adding to the book
- Don’t put your draft in `main` yourself.
- Let me review and add it in.

5. Update your draft after the book changes

   Always pull `main` again so your branch has the newest stuff:
   ```bash
   git pull origin main
   ```

## 6️⃣ Simple Example (You + Ali)

- You finish your part and push it to feature-login.

- Ali finishes his part and pushes it to feature-usermanagement.

 - I will:

   1. Go to main → git pull origin main

   2. Merge Ali’s branch → git merge feature-usermanagement → git push origin main

   3. Merge your branch → git merge feature-login → git push origin main

- Then you and Ali both pull from main again so your branches are up to date.

## 7️⃣ Summary Rules

✅ Always pull before coding

✅ Work only in your branch (your draft)

✅ Push only to your branch

✅ Wait for review before merge

✅ Pull again after merge so you don’t miss changes

❌ Never push directly to main