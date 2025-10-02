import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TaskService } from '../../services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../models/task.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { TaskFormComponent } from '../task-form/task-form.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatMenuModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.scss']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  loading = true;
  searchTerm = '';
  selectedStatus: TaskStatus | 'ALL' = 'ALL';
  selectedPriority: TaskPriority | 'ALL' = 'ALL';

  TaskStatus = TaskStatus;
  TaskPriority = TaskPriority;

  constructor(
    private taskService: TaskService,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.filteredTasks = tasks;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.notificationService.showError('Failed to load tasks');
      }
    });
  }

  filterTasks(): void {
    this.filteredTasks = this.tasks.filter(task => {
      const matchesSearch = !this.searchTerm ||
        task.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (task.description && task.description.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesStatus = this.selectedStatus === 'ALL' ||
        task.status === this.selectedStatus;

      const matchesPriority = this.selectedPriority === 'ALL' ||
        task.priority === this.selectedPriority;

      return matchesSearch && matchesStatus && matchesPriority;
    });
  }

  openTaskDialog(task?: Task): void {
    const dialogRef = this.dialog.open(TaskFormComponent, {
      width: '600px',
      data: task || null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTasks();
      }
    });
  }

  updateTaskStatus(task: Task, status: TaskStatus): void {
    console.log('=== UPDATE TASK STATUS DEBUG ===');
    console.log('Task object:', task);
    console.log('Task ID:', task.id);
    console.log('New status:', status);
    console.log('Status enum value:', TaskStatus[status]);

    if (!task.id) {
      console.error('Task ID is missing');
      alert('Task ID is missing - cannot update status');
      return;
    }

    console.log('Making API call to:', `${this.taskService['apiUrl']}/${task.id}/status?status=${status}`);

    this.taskService.updateTaskStatus(task.id, status).subscribe({
      next: (updatedTask) => {
        console.log('Success! Updated task:', updatedTask);
        this.notificationService.showSuccess('Task status updated successfully');
        this.loadTasks();
      },
      error: (err) => {
        console.error('Error updating task status:', err);
        console.error('Error details:', {
          status: err.status,
          statusText: err.statusText,
          message: err.message,
          error: err.error
        });
        this.notificationService.showError('Failed to update task status');
      }
    });
  }

  deleteTask(task: Task): void {
    if (!task.id) {
      return;
    }

    if (confirm('Are you sure you want to delete "' + task.title + '"?')) {
      this.taskService.deleteTask(task.id).subscribe({
        next: () => {
          this.notificationService.showSuccess('Task deleted successfully');
          this.loadTasks();
        },
        error: () => {
          this.notificationService.showError('Failed to delete task');
        }
      });
    }
  }

  getPriorityColor(priority: TaskPriority): string {
    switch (priority) {
      case TaskPriority.HIGH:
        return '#f44336';
      case TaskPriority.MEDIUM:
        return '#ff9800';
      case TaskPriority.LOW:
        return '#4caf50';
      default:
        return '#9e9e9e';
    }
  }

  getStatusColor(status: TaskStatus): string {
    switch (status) {
      case TaskStatus.TODO:
        return '#2196f3';
      case TaskStatus.IN_PROGRESS:
        return '#ff9800';
      case TaskStatus.COMPLETED:
        return '#4caf50';
      default:
        return '#9e9e9e';
    }
  }
}
