#!/bin/bash
# Add SSH public key to EC2 instance
# Run this via AWS EC2 User Data

set -x

# Add the public key
echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC7vsgAzmxVP78w3m4qGvxdcv88EJg+5GF6lCeFcoDDTmjtsynvplshJccA0Mm87vBJcBOVNxNqrNeFmzAZ1OEzrhYj+07EwbCZTr+66tEPZGytq+rhr+7iJMjhqcYGMoCrHnwwLlSp50Q6+4GNquE2DI9n7GjeX5pX+MWQjMDDFmPM++ftCMPjY+ZVjw3NgamkyEaG9xozF2ELBevr8BR8iN8gmVsO9FFpcqgMR7qH76YnhfuM/fg8zJkW4F8HS4U00xzXyhCSDUKps8toEnmTclnELMQ1rcx3+E/S5Ag1wheyCtbQj+AyQQOOlqBeI6+CsLPbiZTQRba0l/NStkiMin5c4lRd93NPhgT8DcJUXlZT97m5un2l49wO5HWXXEDhbICaaAKUgtKcO0MSqpgtKxp4L3qSwjk8SRKjFIinPuAMseyRFd8ywvo/GWIt5ZC+NkAXNy7VhVmlLoK9VsGfHYyl/xTm6jEd9x0nSggV4sznGFdm3xIua9tZmS+vA5CaZkldDHSaCgL2WAKb55bsvAZCmhyN4mr4iykG8ANG/zcVCHBMcXb9jXfIyGDorp+Iv6O9ixBE5M5Rc/iPHsPMTSzzLYbO/aFU3o7rokKdmKRc03M/Rx7ELCUx2KnWd/aTX5nDh3If5G7ioCAH0Mj2SCK8taPceTgeZkQgHhvYBQ== github-actions@flomad" >> /home/ubuntu/.ssh/authorized_keys

# Ensure correct permissions
chmod 600 /home/ubuntu/.ssh/authorized_keys
chown ubuntu:ubuntu /home/ubuntu/.ssh/authorized_keys

# Start Docker containers
cd /home/ubuntu/docker-app
docker-compose up -d

echo "Setup complete"
