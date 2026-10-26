import React from 'react';
import { motion } from 'framer-motion';
import { Cpu, MapPin, Monitor } from 'lucide-react';
import { mockDevices } from '../mockData';

export default function DeviceTab() {
  const groups = [...new Set(mockDevices.map(d => d.group))];

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6"
      >
        <h2 className="text-2xl font-bold text-white mb-2">Network Devices</h2>
        <p className="text-purple-200">Manage your network switches and routers</p>
      </motion.div>

      {groups.map((group, groupIdx) => {
        const groupDevices = mockDevices.filter(d => d.group === group);
        return (
          <motion.div
            key={group}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 + groupIdx * 0.1 }}
            className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6"
          >
            <h3 className="text-xl font-bold text-white mb-4 capitalize flex items-center gap-2">
              <Monitor size={20} />
              {group.replace('-', ' ')}
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {groupDevices.map((device, idx) => (
                <motion.div
                  key={device.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.1 + idx * 0.05 }}
                  whileHover={{ scale: 1.02, y: -5 }}
                  className="bg-gradient-to-br from-green-500/20 to-emerald-500/20 rounded-xl border border-green-400/30 p-4 hover:border-green-400/50 transition-all"
                >
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <motion.div
                        animate={{ scale: [1, 1.2, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                        className="w-3 h-3 rounded-full bg-green-400"
                      />
                      <span className="font-bold text-white">{device.name}</span>
                    </div>
                    <Cpu size={16} className="text-green-300" />
                  </div>
                  <div className="space-y-2 text-sm">
                    <p className="text-purple-200"><span className="text-purple-300 font-semibold">IP:</span> {device.ip}</p>
                    <p className="text-purple-200"><span className="text-purple-300 font-semibold">Model:</span> {device.model}</p>
                    <p className="text-purple-200"><span className="text-purple-300 font-semibold">OS:</span> {device.os}</p>
                    <p className="flex items-center gap-1 text-purple-200">
                      <MapPin size={14} className="text-blue-300" />
                      {device.site}
                    </p>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>
        );
      })}
    </div>
  );
}
