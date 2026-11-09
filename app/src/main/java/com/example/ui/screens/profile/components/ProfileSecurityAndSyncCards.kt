package com.example.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.firestore.SyncReport
import com.example.data.model.UserProfile
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.util.BiometricAuthHelper

@Composable
fun BiometricSecurityCard(
    isBiometricLockEnabled: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    onRestoreDefaultRoutines: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val biometricStatus = remember(context) { BiometricAuthHelper.checkBiometricSupport(context) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("biometric_security_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Segurança Biometria",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    color = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE) {
                        GrowthGreen.copy(alpha = 0.15f)
                    } else {
                        GrowthOrange.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE) {
                            GrowthGreen.copy(alpha = 0.4f)
                        } else {
                            GrowthOrange.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE) {
                            "Ativo no Aparelho"
                        } else {
                            "Compatível"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE) {
                            GrowthGreen
                        } else {
                            GrowthOrangeLight
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonCardElevated, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bloquear Diários com Biometria",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Exige impressão digital ou face ao entrar nas abas de Treino e Dieta.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isBiometricLockEnabled,
                    onCheckedChange = onToggleBiometricLock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GrowthCyan,
                        uncheckedTrackColor = CarbonBorder
                    ),
                    modifier = Modifier.testTag("biometric_lock_toggle")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (fragmentActivity != null) {
                            BiometricAuthHelper.promptBiometricAuth(
                                activity = fragmentActivity,
                                title = "Teste de Biometria",
                                subtitle = "Sensor biométrico verificado com sucesso",
                                description = "Toque no sensor biométrico do aparelho.",
                                onSuccess = {
                                    onShowToast("Sensor biométrico reconhecido e validado com sucesso!")
                                },
                                onError = { error ->
                                    onShowToast(error)
                                }
                            )
                        } else {
                            onShowToast("Biometria não disponível nesta sessão")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("test_biometric_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CarbonCardElevated,
                        contentColor = GrowthCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Testar Sensor",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onRestoreDefaultRoutines,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("restore_routines_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CarbonCardElevated,
                        contentColor = GrowthOrangeLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = GrowthOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Restaurar ABCDE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun FirestoreSyncCard(
    profile: UserProfile,
    isSyncBusy: Boolean,
    lastSyncReport: SyncReport?,
    onUploadProfile: () -> Unit,
    onDownloadProfile: () -> Unit,
    onSyncAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCloudLinked = profile.firebaseUid.isNotBlank() &&
        !profile.firebaseUid.startsWith("guest_") &&
        !profile.email.endsWith("@growth.local")
    var showDeltaDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_sync_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Backup & Nuvem do Perfil",
                        color = GrowthCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = if (isCloudLinked) GrowthGreen.copy(alpha = 0.15f) else CarbonCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isCloudLinked) GrowthGreen.copy(alpha = 0.4f) else CarbonBorder)
                ) {
                    Text(
                        text = if (isCloudLinked) "Delta Sync Ativo" else "Conta Visitante",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCloudLinked) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = if (isCloudLinked) {
                    "Sincronização inteligente ativa: apenas campos e históricos alterados são transmitidos (o registro mais recente vence)."
                } else {
                    "Seus dados estão salvos neste aparelho. Vincule sua conta Google no topo da tela para liberar o backup e download na nuvem."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Surface(
                color = CarbonCardElevated,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GrowthGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = lastSyncReport?.shortMessage()
                                    ?: if (isCloudLinked) "Sincronização em dia! ✅" else "Modo local pronto",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { showDeltaDetails = !showDeltaDetails },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delta_sync_info_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Ver detalhes de itens enviados vs ignorados",
                                tint = if (showDeltaDetails) GrowthCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showDeltaDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp)
                                .testTag("delta_sync_details_panel"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Resumo do Delta Sync (Mais recente vence):",
                                color = GrowthCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Enviados (modificados): ${lastSyncReport?.successful ?: 0}  |  • Ignorados (sem mudança): ${lastSyncReport?.skipped ?: 0}",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "• Campos de perfil alterados: ${lastSyncReport?.updatedFieldsCount ?: 0}" +
                                    if ((lastSyncReport?.pending ?: 0) > 0) "  |  • Pendentes: ${lastSyncReport?.pending}" else "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onUploadProfile,
                    enabled = !isSyncBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("upload_profile_cloud_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CarbonCardElevated,
                        contentColor = GrowthCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enviar Perfil",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onDownloadProfile,
                    enabled = !isSyncBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("download_profile_cloud_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CarbonCardElevated,
                        contentColor = GrowthCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Baixar Perfil",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onSyncAllData,
                enabled = !isSyncBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("sync_firestore_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrowthOrange,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sincronizar Tudo (Delta Sync)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileFooterAndActionsSection(
    onRestoreBiometrics: () -> Unit,
    onLogout: () -> Unit,
    onRequestResetDatabase: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onRestoreBiometrics,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("restore_biometrics_btn"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan)
        ) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = GrowthCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Restaurar Medidas Originais",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrowthCyan
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("logout_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = GrowthCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Sair da Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
            }

            OutlinedButton(
                onClick = onRequestResetDatabase,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("reset_database_users_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrowthRed.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthRed)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = GrowthRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Zerar Banco", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthRed)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Virtus - Dieta & Treino",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Versão 0.9.0 (Build 1 - Beta)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrowthOrange
            )
        }
    }
}

@Composable
fun ResetDatabaseConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text(
                text = "Excluir Usuários & Zerar Banco?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = "Isso excluirá todos os perfis e credenciais de usuários da base de dados, permitindo iniciar o aplicativo 100% zerado para novos cadastros.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthRed),
                modifier = Modifier.testTag("confirm_wipe_users_btn")
            ) {
                Text("Sim, Excluir Tudo", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
