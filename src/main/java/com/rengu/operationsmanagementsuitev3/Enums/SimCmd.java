package com.rengu.operationsmanagementsuitev3.Enums;

/**
 * @Author YJH
 * @Date 2019/3/20 8:57
 */
public enum SimCmd {
    CMD_ADD_ENTITY,                          //添加兵力实体
    CMD_CHANGE_ENTITY_ATTRIBUTE,             //修改编组关系
    CMD_CASE,                                //特情命令
    CMD_LOAD_XD,                             //加载想定
    CMD_LOAD_NAVI,                           //加载路径文件
    CMD_LOAD_TASK,                           //加载任务信息
    CMD_ENGINE_START,                        //仿真开始
    CMD_ENGINE_SUSPEND,                      //仿真暂停
    CMD_ENGINE_RECOVER,                      //仿真恢复
    CMD_ENGINE_STOP,                         //仿真停止
    CMD_ENGINE_MULTIPLE_SPEED,               //设置仿真速度
    CMD_ENGINE_ADDTHREADS,                   //设置线程数
    CMD_INTERVENE,                           //干预命令
    CMD_PROJECT,                             //设置方案信息
    CMD_RULE,                                //设置规则信息
    CMD_TRACKWAY,                            //设置典型路径信息
    CMD_SC_TRACKWAY,                         //设置特情路径信息
    CMD_SYSTEM_STATUS,                       //获取系统状态
    CMD_LOAD_PROJ,                           //加载流程
    CMD_LOAD_RULE,                           //加载规则
    CMD_SIMULATOR_DATA,                      //加载模拟器数据
    CMD_TYPE_ERROR                           //CMD指令错误
}
