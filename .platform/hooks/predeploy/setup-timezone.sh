#!/bin/bash
echo "time zone for EC2 instance before change: "
timedatectl | grep "Time zone"
echo -e "\n\n resetting timezone to America/Los_Angles ..."
timedatectl set-timezone America/Los_Angeles
echo -e "\n\n time zone for EC2 instance now: "
timedatectl | grep "Time zone"
