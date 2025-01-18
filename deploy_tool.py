import sys
import subprocess
import os
from PyQt6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                            QPushButton, QTextEdit, QLabel, QProgressBar)
from PyQt6.QtCore import QThread, pyqtSignal, Qt

class DeployWorker(QThread):
    output_ready = pyqtSignal(str)
    progress_ready = pyqtSignal(int)
    finished = pyqtSignal()
    error = pyqtSignal(str)

    def __init__(self, project_path):
        super().__init__()
        self.project_path = project_path
        self.tomcat_path = "/opt/homebrew/Cellar/tomcat/11.0.2/libexec"
        self.total_steps = 5
        self.current_step = 0

    def run_command(self, command, shell=False):
        try:
            process = subprocess.Popen(
                command,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                shell=shell,
                text=True
            )
            
            while True:
                output = process.stdout.readline()
                if output == '' and process.poll() is not None:
                    break
                if output:
                    self.output_ready.emit(output.strip())
            
            stderr = process.stderr.read()
            if stderr:
                self.output_ready.emit(f"Error: {stderr}")
            
            return process.poll() == 0
        except Exception as e:
            self.error.emit(str(e))
            return False

    def update_progress(self):
        self.current_step += 1
        progress = int((self.current_step / self.total_steps) * 100)
        self.progress_ready.emit(progress)

    def run(self):
        # 1. Clean and package
        self.output_ready.emit("Step 1: Maven clean and package")
        if not self.run_command(["mvn", "clean", "package"], shell=False):
            self.error.emit("Maven build failed")
            return
        self.update_progress()

        # 2. Stop Tomcat
        self.output_ready.emit("\nStep 2: Stopping Tomcat")
        if not self.run_command(["brew", "services", "stop", "tomcat"], shell=False):
            self.error.emit("Failed to stop Tomcat")
            return
        self.update_progress()

        # 3. Remove old deployment
        self.output_ready.emit("\nStep 3: Removing old deployment")
        webapps_path = os.path.join(self.tomcat_path, "webapps")
        if not self.run_command(f"rm -rf {webapps_path}/ROOT {webapps_path}/ROOT.war", shell=True):
            self.error.emit("Failed to remove old deployment")
            return
        self.update_progress()

        # 4. Copy new WAR file
        self.output_ready.emit("\nStep 4: Copying new WAR file")
        war_file = os.path.join(self.project_path, "target/ROOT.war")
        if not self.run_command(f"cp {war_file} {webapps_path}/", shell=True):
            self.error.emit("Failed to copy WAR file")
            return
        self.update_progress()

        # 5. Start Tomcat
        self.output_ready.emit("\nStep 5: Starting Tomcat")
        if not self.run_command(["brew", "services", "start", "tomcat"], shell=False):
            self.error.emit("Failed to start Tomcat")
            return
        self.update_progress()

        self.output_ready.emit("\nDeployment completed successfully!")
        self.finished.emit()

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Java Web App Deployment Tool")
        self.setGeometry(100, 100, 800, 600)
        
        # Create central widget and layout
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        layout = QVBoxLayout(central_widget)
        
        # Add widgets
        self.status_label = QLabel("Ready to deploy")
        layout.addWidget(self.status_label)
        
        self.progress_bar = QProgressBar()
        layout.addWidget(self.progress_bar)
        
        self.log_text = QTextEdit()
        self.log_text.setReadOnly(True)
        layout.addWidget(self.log_text)
        
        self.deploy_button = QPushButton("Deploy")
        self.deploy_button.clicked.connect(self.start_deployment)
        layout.addWidget(self.deploy_button)
        
        # Initialize worker
        self.project_path = "/Users/apple/APP/java/student_shops"
        self.worker = None

    def start_deployment(self):
        self.deploy_button.setEnabled(False)
        self.log_text.clear()
        self.progress_bar.setValue(0)
        self.status_label.setText("Deploying...")
        
        self.worker = DeployWorker(self.project_path)
        self.worker.output_ready.connect(self.update_log)
        self.worker.progress_ready.connect(self.update_progress)
        self.worker.finished.connect(self.deployment_finished)
        self.worker.error.connect(self.deployment_error)
        self.worker.start()

    def update_log(self, text):
        self.log_text.append(text)
        # Scroll to bottom
        scrollbar = self.log_text.verticalScrollBar()
        scrollbar.setValue(scrollbar.maximum())

    def update_progress(self, value):
        self.progress_bar.setValue(value)

    def deployment_finished(self):
        self.status_label.setText("Deployment completed successfully!")
        self.deploy_button.setEnabled(True)

    def deployment_error(self, error_message):
        self.status_label.setText(f"Error: {error_message}")
        self.deploy_button.setEnabled(True)

def main():
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())

if __name__ == "__main__":
    main()
