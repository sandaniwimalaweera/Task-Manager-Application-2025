import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TaskService } from '../../services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../models/task.model';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.scss']
})
export class TaskFormComponent implements OnInit {
  taskForm!: FormGroup;
  loading = false;
  isEditMode = false;

  statusOptions = Object.values(TaskStatus);
  priorityOptions = Object.values(TaskPriority);

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private notificationService: NotificationService,
    public dialogRef: MatDialogRef<TaskFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Task | null
  ) {
    this.isEditMode = !!data;
  }

  ngOnInit(): void {
    this.taskForm = this.fb.group({
      title: [this.data?.title || '', [Validators.required, Validators.maxLength(100)]],
      description: [this.data?.description || '', [Validators.maxLength(500)]],
      status: [this.data?.status || TaskStatus.TODO, [Validators.required]],
      priority: [this.data?.priority || TaskPriority.MEDIUM, [Validators.required]],
      dueDate: [this.data?.dueDate || null],
      category: [this.data?.category || '']
    });
  }

  onSubmit(): void {
    if (this.taskForm.invalid) {
      return;
    }

    this.loading = true;
    const taskData: Task = this.taskForm.value;

    if (this.isEditMode && this.data?.id) {
      this.taskService.updateTask(this.data.id, taskData).subscribe({
        next: () => {
          this.notificationService.showSuccess('Task updated successfully');
          this.dialogRef.close(true);
        },
        error: () => {
          this.loading = false;
          this.notificationService.showError('Failed to update task');
        }
      });
    } else {
      this.taskService.createTask(taskData).subscribe({
        next: () => {
          this.notificationService.showSuccess('Task created successfully');
          this.dialogRef.close(true);
        },
        error: () => {
          this.loading = false;
          this.notificationService.showError('Failed to create task');
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
